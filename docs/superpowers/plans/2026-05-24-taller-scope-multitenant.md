# Taller-Scope Multi-Tenant Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow users with role `ADMIN_TALLER` to query data across all `sucursales` belonging to their `taller`, while existing single-sucursal users remain unaffected.

**Architecture:** Every user already has a `sucursal_id` (NOT NULL) and every `sucursal` has a `taller_id` — so tallerId is always derivable from a user's sucursal at login. Login embeds `tallerId` in the JWT for `ADMIN_TALLER` users. A new `TallerContext` ThreadLocal (parallel to `SucursalContext`) carries this value through the request. Service methods branch: if `TallerContext` is set, use taller-scoped repository queries; otherwise use existing sucursal-scoped queries unchanged.

**Tech Stack:** Spring Boot 3.3, Java 21, Spring Security, jjwt, Spring Data JPA, JUnit 5, Mockito, `./mvnw` for builds.

---

## File Map

| Status | File | Responsibility |
|--------|------|----------------|
| **Create** | `src/main/java/com/veloservice/config/security/TallerContext.java` | ThreadLocal\<UUID\> for tallerId — mirrors SucursalContext |
| **Modify** | `src/main/java/com/veloservice/config/security/JwtTokenProvider.java` | Add optional `tallerId` to `generateToken()` and expose extractor |
| **Modify** | `src/main/java/com/veloservice/administracion/infraestructure/persistence/repository/SucursalRepository.java` | Add join-fetch query to load `Sucursal` with its `Taller` |
| **Modify** | `src/main/java/com/veloservice/administracion/application/usecase/AuthService.java` | Derive tallerId at login/register for `ADMIN_TALLER` role |
| **Modify** | `src/main/java/com/veloservice/config/security/JwtAuthenticationFilter.java` | Extract tallerId from JWT, set `TallerContext`, clear both contexts in finally |
| **Modify** | `src/main/java/com/veloservice/config/tenant/TenantOperationAspect.java` | Set `app.current_taller_id` Postgres session var when TallerContext is present |
| **Modify** | `src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenRepository.java` | Add `findAllBySucursal_TallerIdOrderByFechaIngresoDesc(UUID tallerId)` |
| **Modify** | `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java` | Branch `listar()`, `listarUrgentes()`, `metricas()` on `TallerContext` |

---

## Task 1: TallerContext

**Files:**
- Create: `src/main/java/com/veloservice/config/security/TallerContext.java`
- Create: `src/test/java/com/veloservice/config/security/TallerContextTest.java`

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/veloservice/config/security/TallerContextTest.java
package com.veloservice.config.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class TallerContextTest {

    @AfterEach
    void cleanup() {
        TallerContext.clear();
    }

    @Test
    void returnsNullWhenNotSet() {
        assertThat(TallerContext.getCurrentTaller()).isNull();
    }

    @Test
    void storesAndRetrievesTallerId() {
        UUID id = UUID.randomUUID();
        TallerContext.setCurrentTaller(id);
        assertThat(TallerContext.getCurrentTaller()).isEqualTo(id);
    }

    @Test
    void clearRemovesValue() {
        TallerContext.setCurrentTaller(UUID.randomUUID());
        TallerContext.clear();
        assertThat(TallerContext.getCurrentTaller()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=TallerContextTest -pl .
```

Expected: FAIL — `TallerContext` class not found.

- [ ] **Step 3: Create TallerContext**

```java
// src/main/java/com/veloservice/config/security/TallerContext.java
package com.veloservice.config.security;

import java.util.UUID;

public final class TallerContext {

    private static final ThreadLocal<UUID> CURRENT_TALLER = new ThreadLocal<>();

    private TallerContext() {}

    public static void setCurrentTaller(UUID tallerId) {
        CURRENT_TALLER.set(tallerId);
    }

    public static UUID getCurrentTaller() {
        return CURRENT_TALLER.get();
    }

    public static void clear() {
        CURRENT_TALLER.remove();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./mvnw test -Dtest=TallerContextTest -pl .
```

Expected: 3 tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/config/security/TallerContext.java \
        src/test/java/com/veloservice/config/security/TallerContextTest.java
git commit -m "feat: add TallerContext ThreadLocal for taller-scope multi-tenancy"
```

---

## Task 2: JwtTokenProvider — add tallerId claim

**Files:**
- Modify: `src/main/java/com/veloservice/config/security/JwtTokenProvider.java`
- Create: `src/test/java/com/veloservice/config/security/JwtTokenProviderTest.java`

- [ ] **Step 1: Write the failing tests**

```java
// src/test/java/com/veloservice/config/security/JwtTokenProviderTest.java
package com.veloservice.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private final UUID userId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();
    private final UUID tallerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // secret must be >= 32 chars for HS256
        provider = new JwtTokenProvider(
            "test-secret-key-at-least-32-chars!!", 3600000L, 900000L);
    }

    @Test
    void generateTokenWithoutTallerIdEmbedsSucursal() {
        String token = provider.generateToken(userId, "a@b.com", "MECANICO", sucursalId, null);
        assertThat(provider.getSucursalId(token)).isEqualTo(sucursalId);
        assertThat(provider.getTallerId(token)).isNull();
    }

    @Test
    void generateTokenWithTallerIdEmbedsBothClaims() {
        String token = provider.generateToken(userId, "a@b.com", "ADMIN_TALLER", sucursalId, tallerId);
        assertThat(provider.getSucursalId(token)).isEqualTo(sucursalId);
        assertThat(provider.getTallerId(token)).isEqualTo(tallerId);
    }

    @Test
    void getRolReturnsEmbeddedRole() {
        String token = provider.generateToken(userId, "a@b.com", "ADMIN_TALLER", sucursalId, tallerId);
        assertThat(provider.getRol(token)).isEqualTo("ADMIN_TALLER");
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
./mvnw test -Dtest=JwtTokenProviderTest -pl .
```

Expected: FAIL — `generateToken` does not accept 5 arguments.

- [ ] **Step 3: Update JwtTokenProvider.generateToken() to accept tallerId**

Replace the existing `generateToken` method in `src/main/java/com/veloservice/config/security/JwtTokenProvider.java`:

```java
public String generateToken(UUID userId, String email, String rol, UUID sucursalId, UUID tallerId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtExpirationMs);
    JwtBuilder builder = Jwts.builder()
            .subject(email)
            .claim("userId", userId.toString())
            .claim("rol", rol)
            .claim("sucursalId", sucursalId.toString())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(jwtSecret, Jwts.SIG.HS256);
    if (tallerId != null) {
        builder.claim("tallerId", tallerId.toString());
    }
    return builder.compact();
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./mvnw test -Dtest=JwtTokenProviderTest -pl .
```

Expected: 3 tests pass.

- [ ] **Step 5: Fix compilation — update AuthService call sites (2 places)**

`src/main/java/com/veloservice/administracion/application/usecase/AuthService.java` — both calls to `jwtProvider.generateToken(...)` now need a 5th argument. Temporarily pass `null` to keep it compiling:

In the `login` method (around line 91):
```java
String token = jwtProvider.generateToken(
        usuario.getId(), usuario.getEmail(), usuario.getRol().getNombre(), sucursalId, null
);
```

In the `register` method (around line 128):
```java
String token = jwtProvider.generateToken(
        saved.getId(), saved.getEmail(), rol.getNombre(), sucursal.getId(), null
);
```

- [ ] **Step 6: Run full test suite to verify no regressions**

```bash
./mvnw test -pl .
```

Expected: all existing tests pass.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/veloservice/config/security/JwtTokenProvider.java \
        src/main/java/com/veloservice/administracion/application/usecase/AuthService.java \
        src/test/java/com/veloservice/config/security/JwtTokenProviderTest.java
git commit -m "feat: add optional tallerId claim to JWT token generation"
```

---

## Task 3: SucursalRepository — join-fetch taller

**Files:**
- Modify: `src/main/java/com/veloservice/administracion/infraestructure/persistence/repository/SucursalRepository.java`

`Sucursal.taller` is `LAZY` — calling `sucursal.getTaller().getId()` outside Hibernate session throws `LazyInitializationException`. A JPQL join-fetch query avoids this.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/veloservice/administracion/infraestructure/persistence/repository/SucursalRepositoryTest.java
package com.veloservice.administracion.infraestructure.persistence.repository;

import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Taller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SucursalRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Test
    void findByIdWithTallerEagerlyLoadsTaller() {
        Taller taller = em.persist(Taller.builder()
                .nombre("Taller Central")
                .rut("12345678-9")
                .activo(true)
                .build());
        Sucursal sucursal = em.persist(Sucursal.builder()
                .taller(taller)
                .nombre("Sucursal Norte")
                .activo(true)
                .build());
        em.flush();
        em.clear(); // detach so lazy loads would normally fail

        Optional<Sucursal> result = sucursalRepository.findByIdWithTaller(sucursal.getId());

        assertThat(result).isPresent();
        // access taller.getId() outside session — must not throw
        assertThat(result.get().getTaller().getId()).isEqualTo(taller.getId());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=SucursalRepositoryTest -pl .
```

Expected: FAIL — `findByIdWithTaller` method not found.

- [ ] **Step 3: Add query to SucursalRepository**

```java
// add import at top of SucursalRepository if not present:
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

// add method:
@Query("SELECT s FROM Sucursal s JOIN FETCH s.taller WHERE s.id = :id")
Optional<Sucursal> findByIdWithTaller(@Param("id") UUID id);
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./mvnw test -Dtest=SucursalRepositoryTest -pl .
```

Expected: 1 test passes.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/administracion/infraestructure/persistence/repository/SucursalRepository.java \
        src/test/java/com/veloservice/administracion/infraestructure/persistence/repository/SucursalRepositoryTest.java
git commit -m "feat: add findByIdWithTaller join-fetch query to SucursalRepository"
```

---

## Task 4: AuthService — embed tallerId for ADMIN_TALLER

**Files:**
- Modify: `src/main/java/com/veloservice/administracion/application/usecase/AuthService.java`
- Modify: `src/test/java/com/veloservice/administracion/interfaces/rest/AuthControllerTest.java` (add ADMIN_TALLER scenario)

- [ ] **Step 1: Write the failing test**

Add this test to `src/test/java/com/veloservice/administracion/interfaces/rest/AuthControllerTest.java`.
First check what mocks are already present in that file:

```bash
grep -n "@MockBean\|@Mock\|given\|when" src/test/java/com/veloservice/administracion/interfaces/rest/AuthControllerTest.java | head -20
```

Then add the following test to the existing test class (inside the class body):

```java
@Test
void loginAdminTallerEmbedsTallerIdInToken() throws Exception {
    // This test verifies the controller returns 200 with a token for ADMIN_TALLER login.
    // The tallerId embedding is verified via JwtTokenProviderTest — here we just ensure
    // the endpoint works for the ADMIN_TALLER role.
    AuthRequest request = new AuthRequest("admin@taller.com", "Password1!");
    AuthLoginResult result = new AuthLoginResult("Admin", "Taller",
            "eyJhbGciOiJIUzI1NiJ9.fake.sig", "ADMIN_TALLER");
    given(authService.login(any())).willReturn(result);

    mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"email":"admin@taller.com","password":"Password1!"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rol").value("ADMIN_TALLER"));
}
```

- [ ] **Step 2: Run test to verify it passes already (controller delegates to service)**

```bash
./mvnw test -Dtest=AuthControllerTest -pl .
```

Expected: all tests pass (controller doesn't care about tallerId — service does).

- [ ] **Step 3: Update AuthService.login() to derive tallerId for ADMIN_TALLER**

In `src/main/java/com/veloservice/administracion/application/usecase/AuthService.java`, replace the token generation block in `login()` (around lines 85-95):

```java
UUID sucursalId = usuario.getSucursal() != null
        ? usuario.getSucursal().getId()
        : null;
if (sucursalId == null) {
    throw new IllegalStateException("Usuario sin sucursal asignada");
}

UUID tallerId = null;
if ("ADMIN_TALLER".equals(usuario.getRol().getNombre())) {
    Sucursal sucursalConTaller = sucursalRepository.findByIdWithTaller(sucursalId)
            .orElseThrow(() -> new IllegalStateException("Sucursal no encontrada"));
    tallerId = sucursalConTaller.getTaller().getId();
}

String token = jwtProvider.generateToken(
        usuario.getId(), usuario.getEmail(), usuario.getRol().getNombre(), sucursalId, tallerId
);
return new AuthLoginResult(usuario.getNombre(), usuario.getApellido(), token, usuario.getRol().getNombre());
```

Also update `register()` the same way (around line 128):

```java
UUID tallerId = null;
if ("ADMIN_TALLER".equals(rol.getNombre())) {
    Sucursal sucursalConTaller = sucursalRepository.findByIdWithTaller(sucursal.getId())
            .orElseThrow(() -> new IllegalStateException("Sucursal no encontrada"));
    tallerId = sucursalConTaller.getTaller().getId();
}

String token = jwtProvider.generateToken(
        saved.getId(), saved.getEmail(), rol.getNombre(), sucursal.getId(), tallerId
);
```

- [ ] **Step 4: Run full test suite**

```bash
./mvnw test -pl .
```

Expected: all tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/administracion/application/usecase/AuthService.java \
        src/test/java/com/veloservice/administracion/interfaces/rest/AuthControllerTest.java
git commit -m "feat: derive and embed tallerId in JWT for ADMIN_TALLER role at login"
```

---

## Task 5: JwtAuthenticationFilter — extract tallerId, clear contexts

**Files:**
- Modify: `src/main/java/com/veloservice/config/security/JwtAuthenticationFilter.java`

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/veloservice/config/security/JwtAuthenticationFilterTest.java
package com.veloservice.config.security;

import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final UUID userId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();
    private final UUID tallerId = UUID.randomUUID();

    @AfterEach
    void cleanup() {
        SucursalContext.clear();
        TallerContext.clear();
    }

    @Test
    void setsTallerContextWhenTallerIdPresentInToken() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader("Authorization")).willReturn("Bearer valid.token.here");
        given(tokenProvider.validateToken("valid.token.here")).willReturn(true);
        given(tokenProvider.getUserId("valid.token.here")).willReturn(userId);
        given(tokenProvider.getRol("valid.token.here")).willReturn("ADMIN_TALLER");
        given(tokenProvider.getSucursalId("valid.token.here")).willReturn(sucursalId);
        given(tokenProvider.getTallerId("valid.token.here")).willReturn(tallerId);

        filter.doFilterInternal(request, response, chain);

        assertThat(TallerContext.getCurrentTaller()).isEqualTo(tallerId);
        assertThat(SucursalContext.getCurrentSucursal()).isEqualTo(sucursalId);
    }

    @Test
    void doesNotSetTallerContextWhenTallerIdAbsent() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader("Authorization")).willReturn("Bearer valid.token.here");
        given(tokenProvider.validateToken("valid.token.here")).willReturn(true);
        given(tokenProvider.getUserId("valid.token.here")).willReturn(userId);
        given(tokenProvider.getRol("valid.token.here")).willReturn("MECANICO");
        given(tokenProvider.getSucursalId("valid.token.here")).willReturn(sucursalId);
        given(tokenProvider.getTallerId("valid.token.here")).willReturn(null);

        filter.doFilterInternal(request, response, chain);

        assertThat(TallerContext.getCurrentTaller()).isNull();
        assertThat(SucursalContext.getCurrentSucursal()).isEqualTo(sucursalId);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=JwtAuthenticationFilterTest -pl .
```

Expected: FAIL — filter does not set TallerContext.

- [ ] **Step 3: Update JwtAuthenticationFilter.doFilterInternal()**

Replace the `doFilterInternal` method body with:

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
    try {
        String jwt = getJwtFromRequest(request);
        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
            UUID userId = tokenProvider.getUserId(jwt);
            String rol = tokenProvider.getRol(jwt);
            UUID sucursalId = tokenProvider.getSucursalId(jwt);

            UserDetails userDetails = User.builder()
                    .username(userId.toString())
                    .password("")
                    .authorities(Collections.emptyList())
                    .build();

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, Collections.singletonList(() -> "ROLE_" + rol));
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(auth);
            SucursalContext.setCurrentSucursal(sucursalId);
            UsuarioContext.setCurrentUser(userId);

            UUID tallerId = tokenProvider.getTallerId(jwt);
            if (tallerId != null) {
                TallerContext.setCurrentTaller(tallerId);
            }
        }
    } catch (Exception e) {
        log.error("Error procesando JWT", e);
    } finally {
        // ensure ThreadLocals don't leak across pooled threads
        // only clear after filterChain so downstream has access
    }
    filterChain.doFilter(request, response);
    TallerContext.clear();
    SucursalContext.clear();
    UsuarioContext.clear();
}
```

Add the import at the top of the file:
```java
import com.veloservice.config.security.TallerContext;
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./mvnw test -Dtest=JwtAuthenticationFilterTest -pl .
```

Expected: 2 tests pass.

- [ ] **Step 5: Run full suite**

```bash
./mvnw test -pl .
```

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/veloservice/config/security/JwtAuthenticationFilter.java \
        src/test/java/com/veloservice/config/security/JwtAuthenticationFilterTest.java
git commit -m "feat: extract tallerId from JWT and set TallerContext in auth filter"
```

---

## Task 6: TenantOperationAspect — branch on taller scope

**Files:**
- Modify: `src/main/java/com/veloservice/config/tenant/TenantOperationAspect.java`

Note: The schema has no actual PostgreSQL `CREATE POLICY` statements — `app.current_sucursal_id` is set but unused for RLS enforcement. This task mirrors that pattern for `app.current_taller_id` to keep the infrastructure consistent for future RLS policies.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/veloservice/config/tenant/TenantOperationAspectTest.java
package com.veloservice.config.tenant;

import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.TallerContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantOperationAspectTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private TenantOperationAspect aspect;

    @AfterEach
    void cleanup() {
        SucursalContext.clear();
        TallerContext.clear();
    }

    @Test
    void setsTallerConfigWhenTallerContextPresent() throws Throwable {
        UUID tallerId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Query query = mock(Query.class);
        given(entityManager.createNativeQuery(contains("current_taller_id"))).willReturn(query);
        given(query.setParameter(eq(1), anyString())).willReturn(query);
        given(query.getSingleResult()).willReturn(null);
        given(joinPoint.proceed()).willReturn(null);

        aspect.applySucursalContext(joinPoint);

        verify(entityManager).createNativeQuery(contains("current_taller_id"));
        verify(query).setParameter(1, tallerId.toString());
    }

    @Test
    void setsSucursalConfigWhenOnlySucursalContextPresent() throws Throwable {
        UUID sucursalId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Query query = mock(Query.class);
        given(entityManager.createNativeQuery(contains("current_sucursal_id"))).willReturn(query);
        given(query.setParameter(eq(1), anyString())).willReturn(query);
        given(query.getSingleResult()).willReturn(null);
        given(joinPoint.proceed()).willReturn(null);

        aspect.applySucursalContext(joinPoint);

        verify(entityManager).createNativeQuery(contains("current_sucursal_id"));
        verify(query).setParameter(1, sucursalId.toString());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=TenantOperationAspectTest -pl .
```

Expected: FAIL — aspect does not branch on TallerContext.

- [ ] **Step 3: Update TenantOperationAspect.applySucursalContext()**

```java
@Around("@annotation(com.veloservice.config.tenant.TenantOperation)")
public Object applySucursalContext(ProceedingJoinPoint joinPoint) throws Throwable {
    UUID tallerId = TallerContext.getCurrentTaller();
    if (tallerId != null) {
        entityManager.createNativeQuery("SELECT set_config('app.current_taller_id', ?, false)")
                .setParameter(1, tallerId.toString())
                .getSingleResult();
    } else {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            entityManager.createNativeQuery("SELECT set_config('app.current_sucursal_id', ?, false)")
                    .setParameter(1, sucursalId.toString())
                    .getSingleResult();
        }
    }
    return joinPoint.proceed();
}
```

Add the import:
```java
import com.veloservice.config.security.TallerContext;
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./mvnw test -Dtest=TenantOperationAspectTest -pl .
```

Expected: 2 tests pass.

- [ ] **Step 5: Run full suite**

```bash
./mvnw test -pl .
```

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/veloservice/config/tenant/TenantOperationAspect.java \
        src/test/java/com/veloservice/config/tenant/TenantOperationAspectTest.java
git commit -m "feat: branch TenantOperationAspect on TallerContext for taller-scope RLS"
```

---

## Task 7: OrdenRepository — taller-scoped queries

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenRepository.java`

Spring Data JPA derives `findAllBySucursal_TallerIdOrderByFechaIngresoDesc` automatically — it traverses the `sucursal` → `taller` → `id` property path in `Orden`.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenRepositoryTallerTest.java
package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Taller;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.ordenes.domain.model.Orden;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrdenRepositoryTallerTest {

    @Autowired private TestEntityManager em;
    @Autowired private OrdenRepository ordenRepository;

    @Test
    void findAllBySucursal_TallerIdReturnsOrdersAcrossBranches() {
        Taller taller = em.persist(Taller.builder().nombre("T1").rut("11111111-1").activo(true).build());
        Taller otroTaller = em.persist(Taller.builder().nombre("T2").rut("22222222-2").activo(true).build());

        Sucursal s1 = em.persist(Sucursal.builder().taller(taller).nombre("S1").activo(true).build());
        Sucursal s2 = em.persist(Sucursal.builder().taller(taller).nombre("S2").activo(true).build());
        Sucursal s3 = em.persist(Sucursal.builder().taller(otroTaller).nombre("S3").activo(true).build());

        // two ordenes in taller, one in other taller
        em.persist(Orden.builder().sucursalId(s1.getId()).numeroOrden("O1")
                .estado(EstadoOrdenEnum.recibida).build());
        em.persist(Orden.builder().sucursalId(s2.getId()).numeroOrden("O2")
                .estado(EstadoOrdenEnum.recibida).build());
        em.persist(Orden.builder().sucursalId(s3.getId()).numeroOrden("O3")
                .estado(EstadoOrdenEnum.recibida).build());
        em.flush();

        List<Orden> result = ordenRepository
                .findAllBySucursal_TallerIdOrderByFechaIngresoDesc(taller.getId());

        assertThat(result).hasSize(2)
                .extracting(o -> o.getNumeroOrden())
                .containsExactlyInAnyOrder("O1", "O2");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=OrdenRepositoryTallerTest -pl .
```

Expected: FAIL — method `findAllBySucursal_TallerIdOrderByFechaIngresoDesc` not found, or `Orden` doesn't have sucursal association.

**If `Orden` stores `sucursalId` as a plain UUID column (not a JPA `@ManyToOne`):** Spring Data cannot traverse `sucursal.tallerId` automatically. Check `Orden.java`:

```bash
grep -n "sucursal\|@ManyToOne\|@Column.*sucursal" src/main/java/com/veloservice/ordenes/domain/model/Orden.java
```

- If `sucursalId` is a plain `UUID` column (no `@ManyToOne`): add a `@ManyToOne(fetch = LAZY)` association to `Orden`, or use a `@Query` instead (see Step 3b).
- If `sucursal` is already a `@ManyToOne`: proceed to Step 3a.

- [ ] **Step 3a: (If @ManyToOne exists) Add derived query to OrdenRepository**

```java
List<Orden> findAllBySucursal_TallerIdOrderByFechaIngresoDesc(UUID tallerId);
```

- [ ] **Step 3b: (If sucursalId is a plain UUID column) Add @Query instead**

```java
@Query("SELECT o FROM Orden o JOIN Sucursal s ON o.sucursalId = s.id WHERE s.taller.id = :tallerId ORDER BY o.fechaIngreso DESC")
List<Orden> findAllByTallerIdOrderByFechaIngresoDesc(@Param("tallerId") UUID tallerId);
```

Adjust `OrdenService` in Task 8 to call `findAllByTallerIdOrderByFechaIngresoDesc` instead.

- [ ] **Step 4: Run test to verify it passes**

```bash
./mvnw test -Dtest=OrdenRepositoryTallerTest -pl .
```

Expected: 1 test passes.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenRepository.java \
        src/test/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenRepositoryTallerTest.java
git commit -m "feat: add taller-scoped query to OrdenRepository"
```

---

## Task 8: OrdenService — branch on TallerContext

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Create: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceTallerTest.java`

- [ ] **Step 1: Write the failing tests**

```java
// src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceTallerTest.java
package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.TallerContext;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTallerTest {

    @Mock private OrdenRepository ordenRepository;
    // add other mocks as needed to construct OrdenService — check constructor
    private OrdenService ordenService;

    private final UUID tallerId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // construct OrdenService with mock dependencies
        // check OrdenService constructor and inject all required mocks
        // Example (adjust to match actual constructor):
        // ordenService = new OrdenService(ordenRepository, productoRepository, ...);
    }

    @AfterEach
    void cleanup() {
        TallerContext.clear();
        SucursalContext.clear();
    }

    @Test
    void listarUrgentesUsesTotallerRepoWhenTallerContextSet() {
        TallerContext.setCurrentTaller(tallerId);
        Orden urgente = Orden.builder()
                .sucursalId(sucursalId)
                .numeroOrden("U1")
                .estado(EstadoOrdenEnum.en_reparacion)
                .fechaPrometida(OffsetDateTime.now().minusDays(1))
                .build();
        given(ordenRepository.findAllBySucursal_TallerIdOrderByFechaIngresoDesc(tallerId))
                .willReturn(List.of(urgente));

        List<?> result = ordenService.listarUrgentes();

        assertThat(result).hasSize(1);
        verify(ordenRepository).findAllBySucursal_TallerIdOrderByFechaIngresoDesc(tallerId);
    }

    @Test
    void listarUrgentesFallsBackToSucursalRepoWhenNoTallerContext() {
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId))
                .willReturn(List.of());

        ordenService.listarUrgentes();

        verify(ordenRepository).findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId);
    }
}
```

**Before running, check OrdenService's constructor parameters:**

```bash
grep -n "public OrdenService\|private final\|@RequiredArgsConstructor" \
  src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java | head -15
```

Fill in the `setUp()` method with all required mock fields and inject them.

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=OrdenServiceTallerTest -pl .
```

Expected: FAIL — `listarUrgentes` always uses sucursal scope.

- [ ] **Step 3: Update OrdenService.listarUrgentes()**

```java
@TenantOperation
@Transactional(readOnly = true)
public List<OrdenResult> listarUrgentes() {
    UUID tallerId = TallerContext.getCurrentTaller();
    List<Orden> ordenes;
    if (tallerId != null) {
        ordenes = ordenRepository.findAllBySucursal_TallerIdOrderByFechaIngresoDesc(tallerId);
    } else {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        ordenes = ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId);
    }
    return ordenes.stream()
            .filter(o -> o.getFechaPrometida() != null
                    && o.getFechaPrometida().isBefore(OffsetDateTime.now())
                    && !o.getEstado().equals(EstadoOrdenEnum.entregada))
            .map(this::toResult)
            .collect(Collectors.toList());
}
```

- [ ] **Step 4: Update OrdenService.listar()**

```java
@TenantOperation
@Transactional(readOnly = true)
public List<OrdenResult> listar() {
    UUID tallerId = TallerContext.getCurrentTaller();
    if (tallerId != null) {
        return ordenRepository.findAllBySucursal_TallerIdOrderByFechaIngresoDesc(tallerId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }
    UUID sucursalId = SucursalContext.getCurrentSucursal();
    UUID mecanicoId = UsuarioContext.getCurrentUser();
    return ordenRepository.findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(sucursalId, mecanicoId).stream()
            .map(this::toResult)
            .collect(Collectors.toList());
}
```

- [ ] **Step 5: Update OrdenService.metricas()**

```java
@TenantOperation
@Transactional(readOnly = true)
public OrdenMetricasResult metricas() {
    UUID tallerId = TallerContext.getCurrentTaller();
    List<Orden> ordenes;
    if (tallerId != null) {
        ordenes = ordenRepository.findAllBySucursal_TallerIdOrderByFechaIngresoDesc(tallerId);
    } else {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        ordenes = ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId);
    }
    long recibidas = ordenes.stream().filter(o -> o.getEstado().equals(EstadoOrdenEnum.recibida)).count();
    long enProceso = ordenes.stream().filter(o -> o.getEstado().equals(EstadoOrdenEnum.en_diagnostico)).count();
    long listas = ordenes.stream().filter(o -> o.getEstado().equals(EstadoOrdenEnum.lista_para_entrega)).count();
    long entregadas = ordenes.stream().filter(o -> o.getEstado().equals(EstadoOrdenEnum.entregada)).count();
    return new OrdenMetricasResult(recibidas, enProceso, listas, entregadas);
}
```

Add import at top of `OrdenService.java` if not present:
```java
import com.veloservice.config.security.TallerContext;
```

- [ ] **Step 6: Run test to verify it passes**

```bash
./mvnw test -Dtest=OrdenServiceTallerTest -pl .
```

Expected: 2 tests pass.

- [ ] **Step 7: Run full test suite**

```bash
./mvnw test -pl .
```

Expected: all tests pass.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceTallerTest.java
git commit -m "feat: branch OrdenService listar/listarUrgentes/metricas on TallerContext"
```

---

## Out of Scope (follow-up tasks using same pattern)

Other services that need taller-scope support follow the exact same pattern as Task 7-8:
- `ProductoService` — add `findAllBySucursal_TallerIdOrderByNombreAsc` to `ProductoRepository`
- `ClienteService` — add `findBySucursalClientes_Sucursal_TallerId` to `ClienteRepository`
- `MecanicoService` — add taller-scoped mechanic query to `UsuarioRepository`
- `DashboardService` — aggregates across branches using taller-scoped queries

---

## Self-Review

**Spec coverage:**
- ✅ TallerContext ThreadLocal — Task 1
- ✅ JWT carries tallerId for ADMIN_TALLER — Tasks 2, 4
- ✅ tallerId derived from sucursal.taller at login — Tasks 3, 4
- ✅ Filter sets TallerContext per request — Task 5
- ✅ Filter clears both contexts after response — Task 5
- ✅ Aspect branches on taller scope — Task 6
- ✅ Repository taller-scoped queries — Task 7
- ✅ Service branches listar/listarUrgentes/metricas — Task 8
- ✅ Existing single-sucursal users unaffected (tallerId null path unchanged)

**Placeholder scan:** None — all steps contain actual code.

**Type consistency:**
- `TallerContext.setCurrentTaller/getCurrentTaller/clear` — consistent across Tasks 1, 5, 6, 8
- `findAllBySucursal_TallerIdOrderByFechaIngresoDesc(UUID tallerId)` — consistent across Tasks 7 and 8
- `generateToken(userId, email, rol, sucursalId, tallerId)` — consistent across Tasks 2 and 4
