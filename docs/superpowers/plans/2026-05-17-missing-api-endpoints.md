# Missing API Endpoints Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement all endpoints defined in `docs/api-endpoints.md` that are missing or misconfigured.

**Architecture:** Each gap is fixed in its own task as a vertical slice: migration → entity/repo → service → controller. All new controller endpoints live in `OrdenController` (no new controllers). Existing `MultimediaController` is kept as-is for backwards compatibility. Comentarios foundation (entity/service) is built first so that `@WebMvcTest(OrdenController.class)` tests can mock it from Task 3 onwards.

**Tech Stack:** Java 21, Spring Boot 3, Spring Data JPA, Flyway, Lombok, JUnit 5 + MockMvc + Mockito (`spring-boot-starter-test`)

---

## File Map

**Create:**
- `src/main/resources/db/migration/V6__create_comentarios.sql`
- `src/main/java/com/veloservice/ordenes/domain/model/Comentario.java`
- `src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/ComentarioRepository.java`
- `src/main/java/com/veloservice/ordenes/application/dto/ComentarioResult.java`
- `src/main/java/com/veloservice/ordenes/application/usecase/ComentarioService.java`
- `src/main/java/com/veloservice/ordenes/interfaces/rest/ComentarioRequest.java`
- `src/main/java/com/veloservice/ordenes/interfaces/rest/ComentarioResponse.java`
- `src/main/java/com/veloservice/ordenes/application/dto/OrdenProductoResult.java`
- `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenProductoResponse.java`
- `src/test/java/com/veloservice/ordenes/application/usecase/ComentarioServiceTest.java`
- `src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenEstadosControllerTest.java`
- `src/test/java/com/veloservice/ordenes/interfaces/rest/ComentarioControllerTest.java`
- `src/test/java/com/veloservice/ordenes/interfaces/rest/MultimediaOrdenControllerTest.java`
- `src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenProductosControllerTest.java`
- `src/test/java/com/veloservice/inventario/interfaces/rest/ProductoSearchControllerTest.java`

**Modify:**
- `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`
- `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- `src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenProductoRepository.java`
- `src/main/java/com/veloservice/ordenes/application/usecase/MultimediaService.java`
- `src/main/java/com/veloservice/inventario/infraestructure/persistence/repository/ProductoRepository.java`
- `src/main/java/com/veloservice/inventario/application/usecase/ProductoService.java`
- `src/main/java/com/veloservice/inventario/interfaces/rest/ProductoController.java`

---

## Task 1: Comentarios — migration + entity + repository

**Files:**
- Create: `src/main/resources/db/migration/V6__create_comentarios.sql`
- Create: `src/main/java/com/veloservice/ordenes/domain/model/Comentario.java`
- Create: `src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/ComentarioRepository.java`

- [ ] **Step 1: Create the migration**

```sql
-- src/main/resources/db/migration/V6__create_comentarios.sql
CREATE TABLE comentarios (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id   UUID NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    texto      TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comentarios_orden ON comentarios(orden_id);
```

- [ ] **Step 2: Create the `Comentario` entity**

```java
// src/main/java/com/veloservice/ordenes/domain/model/Comentario.java
package com.veloservice.ordenes.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "comentarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 3: Create `ComentarioRepository`**

```java
// src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/ComentarioRepository.java
package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.domain.model.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, UUID> {
    List<Comentario> findByOrdenIdOrderByCreatedAtAsc(UUID ordenId);
}
```

- [ ] **Step 4: Verify compilation**

```bash
./mvnw compile -pl . 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add src/main/resources/db/migration/V6__create_comentarios.sql \
        src/main/java/com/veloservice/ordenes/domain/model/Comentario.java \
        src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/ComentarioRepository.java
git commit -m "feat: add comentarios table migration, entity and repository"
```

---

## Task 2: ComentarioService

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/application/dto/ComentarioResult.java`
- Create: `src/main/java/com/veloservice/ordenes/application/usecase/ComentarioService.java`
- Create: `src/test/java/com/veloservice/ordenes/application/usecase/ComentarioServiceTest.java`

- [ ] **Step 1: Create `ComentarioResult` DTO**

```java
// src/main/java/com/veloservice/ordenes/application/dto/ComentarioResult.java
package com.veloservice.ordenes.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class ComentarioResult {
    private UUID id;
    private String autor;
    private String texto;
    private OffsetDateTime creadoEn;
}
```

- [ ] **Step 2: Write the failing test**

```java
// src/test/java/com/veloservice/ordenes/application/usecase/ComentarioServiceTest.java
package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.domain.model.Comentario;
import com.veloservice.ordenes.infraestructure.persistence.repository.ComentarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock private ComentarioRepository comentarioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private ComentarioService comentarioService;

    @Test
    void listarPorOrdenReturnsAuthorName() {
        UUID ordenId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Comentario comentario = Comentario.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .texto("Repuesto solicitado")
                .createdAt(OffsetDateTime.now())
                .build();

        Usuario usuario = new Usuario();
        usuario.setNombre("Juan");
        usuario.setApellido("Pérez");

        when(comentarioRepository.findByOrdenIdOrderByCreatedAtAsc(ordenId))
                .thenReturn(List.of(comentario));
        when(usuarioRepository.findById(usuarioId))
                .thenReturn(Optional.of(usuario));

        List<ComentarioResult> results = comentarioService.listarPorOrden(ordenId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAutor()).isEqualTo("Juan Pérez");
        assertThat(results.get(0).getTexto()).isEqualTo("Repuesto solicitado");
    }

    @Test
    void agregarSavesComentarioWithCurrentUser() {
        UUID ordenId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();

        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellido("Gómez");

        Comentario saved = Comentario.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .texto("Texto de prueba")
                .createdAt(OffsetDateTime.now())
                .build();

        when(comentarioRepository.save(any(Comentario.class))).thenReturn(saved);
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        try (MockedStatic<UsuarioContext> ctx = mockStatic(UsuarioContext.class)) {
            ctx.when(UsuarioContext::getCurrentUser).thenReturn(usuarioId);

            ComentarioResult result = comentarioService.agregar(ordenId, "Texto de prueba");

            assertThat(result.getAutor()).isEqualTo("Ana Gómez");
            verify(comentarioRepository).save(any(Comentario.class));
        }
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

```bash
./mvnw test -Dtest=ComentarioServiceTest -pl . 2>&1 | tail -10
```

Expected: compilation error (`ComentarioService` doesn't exist yet).

- [ ] **Step 4: Create `ComentarioService`**

```java
// src/main/java/com/veloservice/ordenes/application/usecase/ComentarioService.java
package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.domain.model.Comentario;
import com.veloservice.ordenes.infraestructure.persistence.repository.ComentarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ComentarioResult> listarPorOrden(UUID ordenId) {
        return comentarioRepository.findByOrdenIdOrderByCreatedAtAsc(ordenId)
                .stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioResult agregar(UUID ordenId, String texto) {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) throw new IllegalStateException("Usuario no presente en contexto");

        Comentario comentario = Comentario.builder()
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .texto(texto)
                .build();

        return toResult(comentarioRepository.save(comentario));
    }

    private ComentarioResult toResult(Comentario c) {
        String autor = usuarioRepository.findById(c.getUsuarioId())
                .map(u -> u.getNombre() + " " + u.getApellido())
                .orElse("Desconocido");
        return ComentarioResult.builder()
                .id(c.getId())
                .autor(autor)
                .texto(c.getTexto())
                .creadoEn(c.getCreatedAt())
                .build();
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
./mvnw test -Dtest=ComentarioServiceTest -pl . 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/dto/ComentarioResult.java \
        src/main/java/com/veloservice/ordenes/application/usecase/ComentarioService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/ComentarioServiceTest.java
git commit -m "feat: add ComentarioService with listar and agregar"
```

---

## Task 3: Fix `GET /ordenes/estados` — 4-group mapping

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`
- Create: `src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenEstadosControllerTest.java`

The current implementation streams raw `EstadoOrdenEnum` names. The mobile app expects exactly 4 keys.
Mapping: `en_diagnostico | esperando_repuestos | en_reparacion | control_calidad` → `en_proceso`. `cancelada` is omitted.

**Note:** All `@WebMvcTest(OrdenController.class)` tests from this task onwards must mock `ComentarioService` (added in Task 2). It is already on the classpath.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenEstadosControllerTest.java
package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrdenEstadosControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OrdenService ordenService;
    @MockBean private OrdenRepository ordenRepository;
    @MockBean private OrdenProductoRepository ordenProductoRepository;
    @MockBean private MultimediaService multimediaService;
    @MockBean private MultimediaRepository multimediaRepository;
    @MockBean private ComentarioService comentarioService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void estadosReturnsFourGroups() throws Exception {
        var o1 = new com.veloservice.ordenes.domain.model.Orden();
        o1.setEstado(EstadoOrdenEnum.recibida);
        var o2 = new com.veloservice.ordenes.domain.model.Orden();
        o2.setEstado(EstadoOrdenEnum.en_reparacion);
        var o3 = new com.veloservice.ordenes.domain.model.Orden();
        o3.setEstado(EstadoOrdenEnum.en_diagnostico);
        var o4 = new com.veloservice.ordenes.domain.model.Orden();
        o4.setEstado(EstadoOrdenEnum.lista_para_entrega);
        var o5 = new com.veloservice.ordenes.domain.model.Orden();
        o5.setEstado(EstadoOrdenEnum.entregada);

        when(ordenRepository.findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(any(), any()))
            .thenReturn(List.of(o1, o2, o3, o4, o5));

        mockMvc.perform(get("/ordenes/estados"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recibida").value(1))
            .andExpect(jsonPath("$.en_proceso").value(2))
            .andExpect(jsonPath("$.lista_para_entrega").value(1))
            .andExpect(jsonPath("$.entregada").value(1));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=OrdenEstadosControllerTest -pl . 2>&1 | tail -20
```

Expected: assertion failure — response contains raw enum names like `en_reparacion`, `en_diagnostico` instead of `en_proceso`.

- [ ] **Step 3: Replace the `estados()` method in `OrdenController`**

Find and replace the existing `estados()` method (the one annotated `@GetMapping("/estados")`). Also add the `EN_PROCESO_ESTADOS` constant and helper method. Add the import for `EnumSet`:

```java
import java.util.EnumSet;
```

```java
private static final java.util.Set<EstadoOrdenEnum> EN_PROCESO_ESTADOS = EnumSet.of(
    EstadoOrdenEnum.en_diagnostico,
    EstadoOrdenEnum.esperando_repuestos,
    EstadoOrdenEnum.en_reparacion,
    EstadoOrdenEnum.control_calidad
);

@GetMapping("/estados")
public ResponseEntity<Map<String, Long>> estados() {
    UUID sucursalId = SucursalContext.getCurrentSucursal();
    UUID mecanicoId = UsuarioContext.getCurrentUser();
    if (sucursalId == null || mecanicoId == null) {
        return ResponseEntity.ok(Map.of());
    }
    Map<String, Long> estados = ordenRepository
            .findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(sucursalId, mecanicoId)
            .stream()
            .filter(o -> o.getEstado() != EstadoOrdenEnum.cancelada)
            .collect(Collectors.groupingBy(
                o -> toGrupoEstado(o.getEstado()),
                Collectors.counting()
            ));
    return ResponseEntity.ok(estados);
}

private static String toGrupoEstado(EstadoOrdenEnum estado) {
    if (EN_PROCESO_ESTADOS.contains(estado)) return "en_proceso";
    return estado.name();
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
./mvnw test -Dtest=OrdenEstadosControllerTest -pl . 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java \
        src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenEstadosControllerTest.java
git commit -m "fix: map 8 internal estados to 4 mobile groups in GET /ordenes/estados"
```

---

## Task 4: Add `PATCH /ordenes/:id/estado` alias

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`

- [ ] **Step 1: Add import and `@PatchMapping` to `cambiarEstado` in `OrdenController`**

Add import at top of file:
```java
import org.springframework.web.bind.annotation.PatchMapping;
```

Add `@PatchMapping` annotation alongside the existing `@PutMapping` on `cambiarEstado`:

```java
@PutMapping("/{id}/estado")
@PatchMapping("/{id}/estado")
public ResponseEntity<OrdenResponse> cambiarEstado(
        @PathVariable UUID id,
        @Valid @RequestBody EstadoChangeRequest request) {
    return ResponseEntity.ok(OrdenMapper.toResponse(
            ordenService.cambiarEstado(id, OrdenMapper.toEstadoChangeCommand(request))
    ));
}
```

- [ ] **Step 2: Run all tests to verify nothing broke**

```bash
./mvnw test -pl . 2>&1 | tail -15
```

Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java
git commit -m "feat: add PATCH /ordenes/{id}/estado alias alongside existing PUT"
```

---

## Task 5: Comentarios endpoints in `OrdenController`

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/interfaces/rest/ComentarioRequest.java`
- Create: `src/main/java/com/veloservice/ordenes/interfaces/rest/ComentarioResponse.java`
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`
- Create: `src/test/java/com/veloservice/ordenes/interfaces/rest/ComentarioControllerTest.java`

- [ ] **Step 1: Create `ComentarioRequest`**

```java
// src/main/java/com/veloservice/ordenes/interfaces/rest/ComentarioRequest.java
package com.veloservice.ordenes.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComentarioRequest {
    @NotBlank
    private String texto;
}
```

- [ ] **Step 2: Create `ComentarioResponse`**

```java
// src/main/java/com/veloservice/ordenes/interfaces/rest/ComentarioResponse.java
package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ComentarioResponse {
    private UUID id;
    private String autor;
    private String texto;
    @JsonProperty("creadoEn")
    private OffsetDateTime creadoEn;
}
```

- [ ] **Step 3: Write the failing test**

```java
// src/test/java/com/veloservice/ordenes/interfaces/rest/ComentarioControllerTest.java
package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComentarioControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrdenService ordenService;
    @MockBean private OrdenRepository ordenRepository;
    @MockBean private OrdenProductoRepository ordenProductoRepository;
    @MockBean private MultimediaService multimediaService;
    @MockBean private MultimediaRepository multimediaRepository;
    @MockBean private ComentarioService comentarioService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void getComentariosReturnsListForOrden() throws Exception {
        UUID ordenId = UUID.randomUUID();
        ComentarioResult result = ComentarioResult.builder()
                .id(UUID.randomUUID())
                .autor("Juan Pérez")
                .texto("Se detectó desgaste")
                .creadoEn(OffsetDateTime.now())
                .build();

        when(comentarioService.listarPorOrden(ordenId)).thenReturn(List.of(result));

        mockMvc.perform(get("/ordenes/{id}/comentarios", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comentarios[0].autor").value("Juan Pérez"))
                .andExpect(jsonPath("$.comentarios[0].texto").value("Se detectó desgaste"));
    }

    @Test
    void postComentarioCreatesAndReturns() throws Exception {
        UUID ordenId = UUID.randomUUID();
        ComentarioResult result = ComentarioResult.builder()
                .id(UUID.randomUUID())
                .autor("Ana Gómez")
                .texto("Repuesto solicitado")
                .creadoEn(OffsetDateTime.now())
                .build();

        when(comentarioService.agregar(eq(ordenId), any())).thenReturn(result);

        ComentarioRequest req = new ComentarioRequest();
        req.setTexto("Repuesto solicitado");

        mockMvc.perform(post("/ordenes/{id}/comentarios", ordenId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autor").value("Ana Gómez"));
    }
}
```

- [ ] **Step 4: Run test to verify it fails**

```bash
./mvnw test -Dtest=ComentarioControllerTest -pl . 2>&1 | tail -10
```

Expected: 404 Not Found (endpoints don't exist yet).

- [ ] **Step 5: Add `ComentarioService` injection and comment endpoints to `OrdenController`**

Add imports at top of `OrdenController`:
```java
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.interfaces.rest.ComentarioRequest;
import com.veloservice.ordenes.interfaces.rest.ComentarioResponse;
```

Add field (Lombok `@RequiredArgsConstructor` handles constructor injection):
```java
private final ComentarioService comentarioService;
```

Add methods:
```java
@GetMapping("/{id}/comentarios")
public ResponseEntity<Map<String, Object>> listarComentarios(@PathVariable UUID id) {
    var comentarios = comentarioService.listarPorOrden(id).stream()
            .map(c -> ComentarioResponse.builder()
                    .id(c.getId())
                    .autor(c.getAutor())
                    .texto(c.getTexto())
                    .creadoEn(c.getCreadoEn())
                    .build())
            .collect(java.util.stream.Collectors.toList());
    return ResponseEntity.ok(Map.of("comentarios", comentarios));
}

@PostMapping("/{id}/comentarios")
public ResponseEntity<ComentarioResponse> agregarComentario(
        @PathVariable UUID id,
        @Valid @RequestBody ComentarioRequest request) {
    var result = comentarioService.agregar(id, request.getTexto());
    return ResponseEntity.ok(ComentarioResponse.builder()
            .id(result.getId())
            .autor(result.getAutor())
            .texto(result.getTexto())
            .creadoEn(result.getCreadoEn())
            .build());
}
```

- [ ] **Step 6: Run test to verify it passes**

```bash
./mvnw test -Dtest=ComentarioControllerTest -pl . 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/interfaces/rest/ComentarioRequest.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/ComentarioResponse.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java \
        src/test/java/com/veloservice/ordenes/interfaces/rest/ComentarioControllerTest.java
git commit -m "feat: add GET/POST /ordenes/{id}/comentarios endpoints"
```

---

## Task 6: Multimedia endpoints in `OrdenController`

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/MultimediaService.java`
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`
- Create: `src/test/java/com/veloservice/ordenes/interfaces/rest/MultimediaOrdenControllerTest.java`

The existing `MultimediaController` stays unchanged (backwards compatibility). `MultimediaService` gets a new `eliminar(UUID id)` method. `OrdenController` gets `GET`, `POST`, `DELETE` endpoints under `/ordenes/{id}/multimedia`.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/veloservice/ordenes/interfaces/rest/MultimediaOrdenControllerTest.java
package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.enums.TipoArchivoEnum;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class MultimediaOrdenControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrdenService ordenService;
    @MockBean private OrdenRepository ordenRepository;
    @MockBean private OrdenProductoRepository ordenProductoRepository;
    @MockBean private MultimediaService multimediaService;
    @MockBean private MultimediaRepository multimediaRepository;
    @MockBean private ComentarioService comentarioService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void getMultimediaReturnsListForOrden() throws Exception {
        UUID ordenId = UUID.randomUUID();
        MultimediaResult m = MultimediaResult.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(UUID.randomUUID())
                .url("https://storage.example.com/foto1.jpg")
                .tipoArchivo(TipoArchivoEnum.imagen)
                .etapa(EtapaMultimediaEnum.recepcion)
                .createdAt(OffsetDateTime.now())
                .build();

        when(multimediaService.listarPorOrden(ordenId)).thenReturn(List.of(m));

        mockMvc.perform(get("/ordenes/{id}/multimedia", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.multimedia[0].url")
                        .value("https://storage.example.com/foto1.jpg"));
    }

    @Test
    void postMultimediaUploadsAndReturns() throws Exception {
        UUID ordenId = UUID.randomUUID();
        MultimediaResult m = MultimediaResult.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(UUID.randomUUID())
                .url("https://storage.example.com/foto2.jpg")
                .tipoArchivo(TipoArchivoEnum.imagen)
                .etapa(EtapaMultimediaEnum.recepcion)
                .createdAt(OffsetDateTime.now())
                .build();

        when(multimediaService.subir(eq(ordenId), any(), any())).thenReturn(m);

        mockMvc.perform(post("/ordenes/{id}/multimedia", ordenId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "url", "https://storage.example.com/foto2.jpg",
                        "etapa", "recepcion",
                        "tipoArchivo", "imagen"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://storage.example.com/foto2.jpg"));
    }

    @Test
    void deleteMultimediaReturnsOk() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        doNothing().when(multimediaService).eliminar(mediaId);

        mockMvc.perform(delete("/ordenes/{id}/multimedia/{mediaId}", ordenId, mediaId))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=MultimediaOrdenControllerTest -pl . 2>&1 | tail -10
```

Expected: compilation error (`eliminar` not on `MultimediaService`) or 404.

- [ ] **Step 3: Add `eliminar` to `MultimediaService`**

```java
// In MultimediaService.java, add:
@Transactional
public void eliminar(UUID id) {
    multimediaRepository.deleteById(id);
}
```

- [ ] **Step 4: Add multimedia endpoints to `OrdenController`**

Add imports at top of `OrdenController`:
```java
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.interfaces.rest.MultimediaRequest;
import com.veloservice.ordenes.interfaces.mapper.MultimediaMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
```

Add field:
```java
private final MultimediaService multimediaService;
```

Add methods:
```java
@GetMapping("/{id}/multimedia")
public ResponseEntity<Map<String, Object>> listarMultimedia(@PathVariable UUID id) {
    var multimedia = multimediaService.listarPorOrden(id).stream()
            .map(m -> MultimediaResponse.builder()
                    .id(m.getId())
                    .ordenId(m.getOrdenId())
                    .usuarioId(m.getUsuarioId())
                    .url(m.getUrl())
                    .tipoArchivo(m.getTipoArchivo())
                    .etapa(m.getEtapa())
                    .descripcion(m.getDescripcion())
                    .createdAt(m.getCreatedAt())
                    .build())
            .collect(java.util.stream.Collectors.toList());
    return ResponseEntity.ok(Map.of("multimedia", multimedia));
}

@PostMapping("/{id}/multimedia")
public ResponseEntity<MultimediaResponse> subirMultimedia(
        @PathVariable UUID id,
        @Valid @RequestBody MultimediaRequest request) {
    var result = multimediaService.subir(id, request.getEtapa(), MultimediaMapper.toCommand(request));
    return ResponseEntity.ok(MultimediaResponse.builder()
            .id(result.getId())
            .ordenId(result.getOrdenId())
            .usuarioId(result.getUsuarioId())
            .url(result.getUrl())
            .tipoArchivo(result.getTipoArchivo())
            .etapa(result.getEtapa())
            .descripcion(result.getDescripcion())
            .createdAt(result.getCreatedAt())
            .build());
}

@DeleteMapping("/{id}/multimedia/{mediaId}")
public ResponseEntity<Map<String, Object>> eliminarMultimedia(
        @PathVariable UUID id,
        @PathVariable UUID mediaId) {
    multimediaService.eliminar(mediaId);
    return ResponseEntity.ok(Map.of());
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
./mvnw test -Dtest=MultimediaOrdenControllerTest -pl . 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/MultimediaService.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java \
        src/test/java/com/veloservice/ordenes/interfaces/rest/MultimediaOrdenControllerTest.java
git commit -m "feat: add GET/POST/DELETE /ordenes/{id}/multimedia endpoints"
```

---

## Task 7: `GET /ordenes/:id/productos`

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/application/dto/OrdenProductoResult.java`
- Create: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenProductoResponse.java`
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`
- Create: `src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenProductosControllerTest.java`

- [ ] **Step 1: Create `OrdenProductoResult` DTO**

```java
// src/main/java/com/veloservice/ordenes/application/dto/OrdenProductoResult.java
package com.veloservice.ordenes.application.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrdenProductoResult {
    private UUID id;
    private UUID productoId;
    private String nombre;
    private String sku;
    private Integer cantidad;
    private BigDecimal precioVenta;
}
```

- [ ] **Step 2: Create `OrdenProductoResponse`**

```java
// src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenProductoResponse.java
package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class OrdenProductoResponse {
    private UUID id;
    @JsonProperty("productoId")
    private UUID productoId;
    private String nombre;
    private String sku;
    private Integer cantidad;
    @JsonProperty("precioVenta")
    private BigDecimal precioVenta;
}
```

- [ ] **Step 3: Write the failing test**

```java
// src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenProductosControllerTest.java
package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrdenProductosControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OrdenService ordenService;
    @MockBean private OrdenRepository ordenRepository;
    @MockBean private OrdenProductoRepository ordenProductoRepository;
    @MockBean private MultimediaService multimediaService;
    @MockBean private MultimediaRepository multimediaRepository;
    @MockBean private ComentarioService comentarioService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void getProductosReturnsListForOrden() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();

        OrdenProductoResult r = OrdenProductoResult.builder()
                .id(UUID.randomUUID())
                .productoId(productoId)
                .nombre("Cadena Shimano HG601")
                .sku("SHM-HG601-11")
                .cantidad(1)
                .precioVenta(new BigDecimal("18900"))
                .build();

        when(ordenService.listarProductosPorOrden(ordenId)).thenReturn(List.of(r));

        mockMvc.perform(get("/ordenes/{id}/productos", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos[0].nombre").value("Cadena Shimano HG601"))
                .andExpect(jsonPath("$.productos[0].sku").value("SHM-HG601-11"))
                .andExpect(jsonPath("$.productos[0].cantidad").value(1));
    }

    @Test
    void deleteProductoReturnsOk() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();

        mockMvc.perform(delete("/ordenes/{id}/productos/{productoId}", ordenId, productoId))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 4: Run test to verify it fails**

```bash
./mvnw test -Dtest=OrdenProductosControllerTest -pl . 2>&1 | tail -10
```

Expected: compilation error (`listarProductosPorOrden` not defined on `OrdenService`).

- [ ] **Step 5: Add `listarProductosPorOrden` to `OrdenService`**

Add import at top of `OrdenService`:
```java
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
```

Add method:
```java
@TenantOperation
@Transactional(readOnly = true)
public List<OrdenProductoResult> listarProductosPorOrden(UUID ordenId) {
    return ordenProductoRepository.findByOrdenId(ordenId).stream()
            .map(op -> {
                var producto = productoRepository.findById(op.getProductoId()).orElse(null);
                return OrdenProductoResult.builder()
                        .id(op.getId())
                        .productoId(op.getProductoId())
                        .nombre(producto != null ? producto.getNombre() : "")
                        .sku(producto != null ? producto.getSku() : "")
                        .cantidad(op.getCantidad())
                        .precioVenta(op.getPrecioVentaSnapshot())
                        .build();
            })
            .collect(Collectors.toList());
}
```

- [ ] **Step 6: Add `GET /ordenes/{id}/productos` endpoint to `OrdenController`**

Add import:
```java
import com.veloservice.ordenes.interfaces.rest.OrdenProductoResponse;
```

Add method:
```java
@GetMapping("/{id}/productos")
public ResponseEntity<Map<String, Object>> listarProductos(@PathVariable UUID id) {
    var productos = ordenService.listarProductosPorOrden(id).stream()
            .map(r -> OrdenProductoResponse.builder()
                    .id(r.getId())
                    .productoId(r.getProductoId())
                    .nombre(r.getNombre())
                    .sku(r.getSku())
                    .cantidad(r.getCantidad())
                    .precioVenta(r.getPrecioVenta())
                    .build())
            .collect(java.util.stream.Collectors.toList());
    return ResponseEntity.ok(Map.of("productos", productos));
}
```

- [ ] **Step 7: Run test to verify it passes**

```bash
./mvnw test -Dtest=OrdenProductosControllerTest -pl . 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/dto/OrdenProductoResult.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenProductoResponse.java \
        src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java \
        src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenProductosControllerTest.java
git commit -m "feat: add GET /ordenes/{id}/productos endpoint"
```

---

## Task 8: `DELETE /ordenes/:id/productos/:productoId`

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenProductoRepository.java`
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`

The `deleteProductoReturnsOk` test was already written in Task 7's `OrdenProductosControllerTest`. This task wires up the service logic so the delete call does real work.

- [ ] **Step 1: Add `findByOrdenIdAndProductoId` to `OrdenProductoRepository`**

```java
// In OrdenProductoRepository.java, add:
import java.util.Optional;

Optional<OrdenProducto> findByOrdenIdAndProductoId(UUID ordenId, UUID productoId);
```

- [ ] **Step 2: Add `eliminarProducto` to `OrdenService`**

Add method:
```java
@TenantOperation
@Transactional
public void eliminarProducto(UUID ordenId, UUID productoId) {
    UUID sucursalId = SucursalContext.getCurrentSucursal();
    UUID usuarioId = UsuarioContext.getCurrentUser();

    var ordenProducto = ordenProductoRepository.findByOrdenIdAndProductoId(ordenId, productoId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la orden"));

    if (!Boolean.TRUE.equals(ordenProducto.getProporcionadoPorCliente())) {
        var producto = productoRepository.findByIdAndSucursalId(productoId, sucursalId)
                .orElse(null);
        if (producto != null) {
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior + ordenProducto.getCantidad());
            productoRepository.save(producto);

            var movimiento = com.veloservice.inventario.domain.model.MovimientoStock.builder()
                    .productoId(productoId)
                    .ordenId(ordenId)
                    .usuarioId(usuarioId)
                    .tipo(TipoMovimientoEnum.entrada)
                    .cantidad(ordenProducto.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockPosterior(producto.getStock())
                    .motivo("Devolución por eliminación de orden")
                    .build();
            movimientoStockRepository.save(movimiento);
        }
    }

    ordenProductoRepository.delete(ordenProducto);
}
```

- [ ] **Step 3: Add `DELETE /ordenes/{id}/productos/{productoId}` to `OrdenController`**

Add method:
```java
@DeleteMapping("/{id}/productos/{productoId}")
public ResponseEntity<Map<String, Object>> eliminarProducto(
        @PathVariable UUID id,
        @PathVariable UUID productoId) {
    ordenService.eliminarProducto(id, productoId);
    return ResponseEntity.ok(Map.of());
}
```

- [ ] **Step 4: Run all tests**

```bash
./mvnw test -pl . 2>&1 | tail -15
```

Expected: `BUILD SUCCESS`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenProductoRepository.java \
        src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java
git commit -m "feat: add DELETE /ordenes/{id}/productos/{productoId} with stock restore"
```

---

## Task 9: `GET /productos?search=<query>`

**Files:**
- Modify: `src/main/java/com/veloservice/inventario/infraestructure/persistence/repository/ProductoRepository.java`
- Modify: `src/main/java/com/veloservice/inventario/application/usecase/ProductoService.java`
- Modify: `src/main/java/com/veloservice/inventario/interfaces/rest/ProductoController.java`
- Create: `src/test/java/com/veloservice/inventario/interfaces/rest/ProductoSearchControllerTest.java`

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/veloservice/inventario/interfaces/rest/ProductoSearchControllerTest.java
package com.veloservice.inventario.interfaces.rest;

import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.inventario.application.dto.ProductoResult;
import com.veloservice.inventario.application.usecase.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoSearchControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ProductoService productoService;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    @Test
    void searchReturnsMatchingProductos() throws Exception {
        ProductoResult r = ProductoResult.builder()
                .id(UUID.randomUUID())
                .nombre("Cadena Shimano HG601")
                .sku("SHM-HG601-11")
                .precioCosto(new BigDecimal("9000"))
                .precioVenta(new BigDecimal("18900"))
                .stock(4)
                .build();

        when(productoService.buscar("shimano")).thenReturn(List.of(r));

        mockMvc.perform(get("/productos").param("search", "shimano"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos[0].nombre").value("Cadena Shimano HG601"));
    }

    @Test
    void listarWithoutSearchReturnsAllProductos() throws Exception {
        when(productoService.listar()).thenReturn(List.of());

        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./mvnw test -Dtest=ProductoSearchControllerTest -pl . 2>&1 | tail -10
```

Expected: compilation error (`buscar` not defined on `ProductoService`).

- [ ] **Step 3: Add search query to `ProductoRepository`**

Add imports and method:
```java
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Query("SELECT p FROM Producto p WHERE p.sucursalId = :sucursalId AND " +
       "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
       " LOWER(p.sku)    LIKE LOWER(CONCAT('%', :q, '%')))")
List<Producto> searchBySucursalId(@Param("sucursalId") UUID sucursalId, @Param("q") String q);
```

- [ ] **Step 4: Add `buscar` to `ProductoService`**

Add after `listar()`:
```java
@TenantOperation
@Transactional(readOnly = true)
public List<ProductoResult> buscar(String query) {
    UUID sucursalId = SucursalContext.getCurrentSucursal();
    if (sucursalId == null) return List.of();
    return productoRepository.searchBySucursalId(sucursalId, query).stream()
            .map(producto -> toResult(producto, resolveCategoriaNombre(producto.getCategoriaId())))
            .collect(Collectors.toList());
}
```

- [ ] **Step 5: Update `ProductoController.listar()` to accept `?search=`**

Add import:
```java
import org.springframework.web.bind.annotation.RequestParam;
```

Replace the existing `listar()` method:
```java
@GetMapping
public ResponseEntity<Map<String, Object>> listar(
        @RequestParam(required = false) String search) {
    List<ProductoResponse> productos;
    if (search != null && !search.isBlank()) {
        productos = ProductoMapper.toResponseList(productoService.buscar(search));
    } else {
        productos = ProductoMapper.toResponseList(productoService.listar());
    }
    return ResponseEntity.ok(Map.of(
            "total", productos.size(),
            "productos", productos
    ));
}
```

- [ ] **Step 6: Run test to verify it passes**

```bash
./mvnw test -Dtest=ProductoSearchControllerTest -pl . 2>&1 | tail -10
```

Expected: `BUILD SUCCESS`

- [ ] **Step 7: Run all tests**

```bash
./mvnw test -pl . 2>&1 | tail -15
```

Expected: `BUILD SUCCESS`

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/veloservice/inventario/infraestructure/persistence/repository/ProductoRepository.java \
        src/main/java/com/veloservice/inventario/application/usecase/ProductoService.java \
        src/main/java/com/veloservice/inventario/interfaces/rest/ProductoController.java \
        src/test/java/com/veloservice/inventario/interfaces/rest/ProductoSearchControllerTest.java
git commit -m "feat: add ?search= param to GET /productos with JPQL text search"
```

---

## Spec Coverage Check

| Spec requirement | Task |
|---|---|
| Comentarios DB migration | Task 1 |
| `Comentario` entity + `ComentarioRepository` | Task 1 |
| `ComentarioService` with author name resolution | Task 2 |
| `GET /ordenes/estados` 4-group mapping | Task 3 |
| `PATCH /ordenes/:id/estado` | Task 4 |
| `GET /ordenes/:id/comentarios` | Task 5 |
| `POST /ordenes/:id/comentarios` | Task 5 |
| `GET /ordenes/:id/multimedia` | Task 6 |
| `POST /ordenes/:id/multimedia` | Task 6 |
| `DELETE /ordenes/:id/multimedia/:mediaId` | Task 6 |
| `GET /ordenes/:id/productos` | Task 7 |
| `DELETE /ordenes/:id/productos/:productoId` + stock restore | Task 8 |
| `GET /productos?search=<query>` | Task 9 |
