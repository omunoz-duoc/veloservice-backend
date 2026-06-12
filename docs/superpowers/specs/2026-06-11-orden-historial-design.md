# Orden Historial (Audit Trail) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Record a unified, user-attributed audit trail of every mutation to a work order (estado change, product add/update/remove, service add/update/remove, order-field edit, multimedia add/remove) in a new `orden_historial` table, populated from the backend service layer.

**Architecture:** Strategy B (backend service layer). A new `OrdenHistorialService.registrar(...)` is called inside the existing centralized mutation helpers of `OrdenService` (`aplicarCambioEstado`, `agregarProductosAOrden`, `actualizarProductosAOrden`, `eliminarProductosAOrden`, `agregarServiciosAOrden`, `actualizarServiciosAOrden`, `eliminarServiciosAOrden`, `agregarMultimedia`, `confirmarMultimedia`, and order-field edits in `actualizar`). Because both the dedicated endpoints and the unified `PATCH /ordenes/{id}` route through these same helpers, a single hook layer covers all entry points. Each event records `usuario_id` (from `UsuarioContext`), an `accion`, the affected entity reference, and a flexible `JSONB` `detalle`. The write happens inside the same `@Transactional` boundary as the mutation, so a failed mutation rolls the history back with it. `orden_estados` is left untouched; history references the estado change as a lightweight event.

**Tech Stack:** Java 17, Spring Boot 3.3.0, Spring Data JPA / Hibernate 6.5 (`@JdbcTypeCode(SqlTypes.JSON)` for `jsonb`), PostgreSQL, Flyway, Jackson, JUnit 5 + Mockito + AssertJ.

---

## Design Reference (read before starting)

**Centralized hook points** (all in `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`):

| Mutation | Helper method | Called by |
|----------|---------------|-----------|
| Estado change | `aplicarCambioEstado(...)` (~line 1074) | `cambiarEstado`, `actualizar` |
| Product add | `agregarProductosAOrden(...)` (~line 655) | `agregarProductos`, `actualizar` |
| Product update | `actualizarProductosAOrden(...)` (~line 710) | `actualizar` |
| Product remove | `eliminarProductosAOrden(...)` (~line 740) | `actualizar` |
| Service add | `agregarServiciosAOrden(...)` (~line 767) | `agregarServicios`, `actualizar` |
| Service update | `actualizarServiciosAOrden(...)` (~line 805) | `actualizar` |
| Service remove | `eliminarServiciosAOrden(...)` (~line 834) | `actualizar` |
| Multimedia add (multipart) | `agregarMultimedia(...)` (~line 412) | `agregarMultimedia` endpoint |
| Multimedia add (presigned) | `confirmarMultimedia(...)` (~line 462) | `confirmarMultimedia` endpoint |
| Order field edit | inline in `actualizar(...)` (~line 554) | `PATCH /ordenes/{id}` |

**Not yet existing (note for future, no hook now):** there are no "remove multimedia" or "remove order" endpoints. When added, call `registrar` with `MULTIMEDIA_QUITADA`.

**User context:** `UsuarioContext.getCurrentUser()` returns the actor `UUID` (may be `null` in non-request contexts; the helper `requerirUsuarioContext()` throws if required). History writes use `UsuarioContext.getCurrentUser()` directly and tolerate `null` (stored as nullable `usuario_id`).

**Existing entity/repo patterns to mirror:**
- Entity: `src/main/java/com/veloservice/ordenes/domain/model/OrdenEstado.java` (Lombok `@Builder`, `@Table` + `@Index`).
- Repository projection: `OrdenEstadoRepository.findResultByOrdenId` (JPQL `SELECT new ...Result(...)` with `LEFT JOIN Usuario`).
- Result DTO: `src/main/java/com/veloservice/ordenes/application/dto/OrdenEstadoResult.java` (Java record).

---

## File Structure

**Create:**
- `src/main/resources/db/migration/V7__create_orden_historial.sql` — table + indexes.
- `src/main/java/com/veloservice/ordenes/domain/AccionHistorialEnum.java` — action vocabulary.
- `src/main/java/com/veloservice/ordenes/domain/model/OrdenHistorial.java` — JPA entity.
- `src/main/java/com/veloservice/ordenes/application/dto/OrdenHistorialResult.java` — read projection record.
- `src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenHistorialRepository.java` — repo + projection query.
- `src/main/java/com/veloservice/ordenes/application/usecase/OrdenHistorialService.java` — `registrar(...)` write API.
- `src/main/java/com/veloservice/ordenes/interfaces/rest/dto/OrdenHistorialResponse.java` — REST response record.
- `src/test/java/com/veloservice/ordenes/application/usecase/OrdenHistorialServiceTest.java` — unit tests for the writer.
- `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java` — verifies hooks fire on mutations.

**Modify:**
- `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java` — add dependency + hook calls.
- `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java` — `GET /ordenes/{id}/historial` endpoint.
- `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceCreateScopeTest.java` — add `@Mock OrdenHistorialService` and pass to constructor.

---

## Task 1: Flyway migration V7 — orden_historial table

**Files:**
- Create: `src/main/resources/db/migration/V7__create_orden_historial.sql`

- [ ] **Step 1: Create the migration file**

```sql
-- Audit trail unificado de modificaciones a una orden de trabajo.
-- Estrategia: poblado desde el service layer (no triggers) para capturar usuario e intención.
CREATE TABLE IF NOT EXISTS orden_historial (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id    UUID         NOT NULL REFERENCES ordenes(id)  ON DELETE CASCADE,
    usuario_id  UUID         REFERENCES usuarios(id)          ON DELETE SET NULL,
    accion      VARCHAR(40)  NOT NULL,
    entidad     VARCHAR(40),
    entidad_id  UUID,
    detalle     JSONB,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orden_historial_orden
    ON orden_historial(orden_id, created_at);
```

- [ ] **Step 2: Verify migration applies cleanly**

Run: `./mvnw -q -Dtest=OrdenRepositoryTallerTest test` (any test that boots the Flyway-migrated context).
Expected: PASS — Flyway applies V7 with no checksum/ordering errors. If the project uses a Testcontainers/H2 profile that does not run Flyway, instead run `./mvnw -q flyway:validate` if configured, otherwise rely on Task 12 manual verification.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/db/migration/V7__create_orden_historial.sql
git commit -m "feat(ordenes): add orden_historial audit table (V7 migration)"
```

---

## Task 2: AccionHistorialEnum

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/domain/AccionHistorialEnum.java`

- [ ] **Step 1: Create the enum**

```java
package com.veloservice.ordenes.domain;

/**
 * Vocabulario de acciones registradas en orden_historial.
 * El valor name() se persiste en la columna accion (VARCHAR).
 */
public enum AccionHistorialEnum {
    ESTADO_CAMBIADO,
    ORDEN_EDITADA,
    PRODUCTO_AGREGADO,
    PRODUCTO_MODIFICADO,
    PRODUCTO_QUITADO,
    SERVICIO_AGREGADO,
    SERVICIO_MODIFICADO,
    SERVICIO_QUITADO,
    MULTIMEDIA_AGREGADA,
    MULTIMEDIA_QUITADA
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./mvnw -q compile`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/domain/AccionHistorialEnum.java
git commit -m "feat(ordenes): add AccionHistorialEnum"
```

---

## Task 3: OrdenHistorial entity

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/domain/model/OrdenHistorial.java`

- [ ] **Step 1: Create the entity**

`detalle` is mapped as a `String` carrying serialized JSON; `@JdbcTypeCode(SqlTypes.JSON)` makes Hibernate 6 bind it to the `jsonb` column.

```java
package com.veloservice.ordenes.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Registro de auditoría unificado de modificaciones a una orden de trabajo.
 */
@Entity
@Table(
    name = "orden_historial",
    indexes = {
        @Index(name = "idx_orden_historial_orden", columnList = "orden_id, created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "accion", nullable = false, length = 40)
    private String accion;

    @Column(name = "entidad", length = 40)
    private String entidad;

    @Column(name = "entidad_id")
    private UUID entidadId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "detalle")
    private String detalle;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
```

- [ ] **Step 2: Verify it compiles**

Run: `./mvnw -q compile`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/domain/model/OrdenHistorial.java
git commit -m "feat(ordenes): add OrdenHistorial entity"
```

---

## Task 4: OrdenHistorialResult DTO + repository

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/application/dto/OrdenHistorialResult.java`
- Create: `src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenHistorialRepository.java`

- [ ] **Step 1: Create the result record**

```java
package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenHistorialResult(
        UUID id,
        UUID ordenId,
        String accion,
        String entidad,
        UUID entidadId,
        String detalle,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
) {}
```

- [ ] **Step 2: Create the repository**

```java
package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.application.dto.OrdenHistorialResult;
import com.veloservice.ordenes.domain.model.OrdenHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdenHistorialRepository extends JpaRepository<OrdenHistorial, UUID> {

    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.OrdenHistorialResult(
            h.id,
            h.ordenId,
            h.accion,
            h.entidad,
            h.entidadId,
            h.detalle,
            h.usuarioId,
            COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sistema'),
            h.createdAt
        )
        FROM OrdenHistorial h
        LEFT JOIN com.veloservice.auth.domain.model.Usuario u ON u.id = h.usuarioId
        WHERE h.ordenId = :ordenId
        ORDER BY h.createdAt DESC
        """)
    List<OrdenHistorialResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
}
```

- [ ] **Step 3: Verify it compiles**

Run: `./mvnw -q compile`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/dto/OrdenHistorialResult.java \
        src/main/java/com/veloservice/ordenes/infraestructure/persistence/repository/OrdenHistorialRepository.java
git commit -m "feat(ordenes): add OrdenHistorialResult and repository projection"
```

---

## Task 5: OrdenHistorialService.registrar (TDD)

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenHistorialService.java`
- Test: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenHistorialServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.domain.AccionHistorialEnum;
import com.veloservice.ordenes.domain.model.OrdenHistorial;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenHistorialRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrdenHistorialServiceTest {

    @Mock private OrdenHistorialRepository repository;

    @AfterEach
    void clearContext() {
        UsuarioContext.clear();
    }

    @Test
    void registrar_persistsEventWithActorAndSerializedDetalle() {
        UUID ordenId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID entidadId = UUID.randomUUID();
        UsuarioContext.setCurrentUser(usuarioId);

        OrdenHistorialService service = new OrdenHistorialService(repository);

        service.registrar(ordenId, AccionHistorialEnum.PRODUCTO_AGREGADO, "producto", entidadId,
                Map.of("nombre", "Cadena KMC", "cantidad", 2));

        ArgumentCaptor<OrdenHistorial> captor = ArgumentCaptor.forClass(OrdenHistorial.class);
        verify(repository).save(captor.capture());
        OrdenHistorial saved = captor.getValue();
        assertThat(saved.getOrdenId()).isEqualTo(ordenId);
        assertThat(saved.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(saved.getAccion()).isEqualTo("PRODUCTO_AGREGADO");
        assertThat(saved.getEntidad()).isEqualTo("producto");
        assertThat(saved.getEntidadId()).isEqualTo(entidadId);
        assertThat(saved.getDetalle()).contains("\"nombre\":\"Cadena KMC\"").contains("\"cantidad\":2");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void registrar_toleratesNullActorAndNullDetalle() {
        UUID ordenId = UUID.randomUUID();
        OrdenHistorialService service = new OrdenHistorialService(repository);

        service.registrar(ordenId, AccionHistorialEnum.ESTADO_CAMBIADO, null, null, null);

        ArgumentCaptor<OrdenHistorial> captor = ArgumentCaptor.forClass(OrdenHistorial.class);
        verify(repository).save(captor.capture());
        OrdenHistorial saved = captor.getValue();
        assertThat(saved.getUsuarioId()).isNull();
        assertThat(saved.getDetalle()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=OrdenHistorialServiceTest test`
Expected: FAIL — `OrdenHistorialService` does not exist (compilation error).

- [ ] **Step 3: Write the implementation**

```java
package com.veloservice.ordenes.application.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.domain.AccionHistorialEnum;
import com.veloservice.ordenes.domain.model.OrdenHistorial;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenHistorialRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Punto único de escritura del audit trail de órdenes (orden_historial).
 * Se invoca dentro de la misma transacción que la mutación; si la mutación falla, el registro se revierte con ella.
 */
@Service
public class OrdenHistorialService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OrdenHistorialRepository repository;

    public OrdenHistorialService(OrdenHistorialRepository repository) {
        this.repository = repository;
    }

    /**
     * Registra un evento de historial para una orden.
     *
     * @param ordenId   orden afectada (requerido)
     * @param accion    tipo de acción
     * @param entidad   tipo de entidad afectada ('producto','servicio','multimedia','orden') o null
     * @param entidadId id del registro afectado o null
     * @param detalle   datos arbitrarios (antes/despues, nombre, cantidad...) serializados a JSON, o null
     */
    public void registrar(UUID ordenId,
                          AccionHistorialEnum accion,
                          String entidad,
                          UUID entidadId,
                          Map<String, Object> detalle) {
        repository.save(OrdenHistorial.builder()
                .ordenId(ordenId)
                .usuarioId(UsuarioContext.getCurrentUser())
                .accion(accion.name())
                .entidad(entidad)
                .entidadId(entidadId)
                .detalle(serializar(detalle))
                .createdAt(OffsetDateTime.now())
                .build());
    }

    private String serializar(Map<String, Object> detalle) {
        if (detalle == null || detalle.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(detalle);
        } catch (Exception ex) {
            // El historial nunca debe romper la operación de negocio.
            return null;
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=OrdenHistorialServiceTest test`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenHistorialService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenHistorialServiceTest.java
git commit -m "feat(ordenes): add OrdenHistorialService.registrar with tests"
```

---

## Task 6: Wire OrdenHistorialService into OrdenService

> **Note:** `OrdenService` has a primary `@Autowired` constructor (~line 121) and a secondary constructor (~line 169) used by tests. Both must accept and assign the new dependency. The field must be declared near the other repository fields (~lines 95-120).

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Modify: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceCreateScopeTest.java`

- [ ] **Step 1: Add the field**

In the field block (after `private final R2Properties r2Properties;` or the last private final field, before the constructor at ~line 121), add:

```java
    private final OrdenHistorialService ordenHistorialService;
```

- [ ] **Step 2: Add the parameter to the primary constructor**

In the primary constructor signature (~line 121), add a parameter (place it last, after `R2Properties r2Properties`):

```java
            OrdenHistorialService ordenHistorialService
```

And in the assignment block (after `this.r2Properties = r2Properties;` ~line 166) add:

```java
        this.ordenHistorialService = ordenHistorialService;
```

- [ ] **Step 3: Add the parameter to the secondary constructor**

The secondary constructor (~line 169) delegates to the primary (likely via `this(...)`). Add `ordenHistorialService` as the final argument in its parameter list and pass it through. If the secondary constructor builds the `this(...)` call with explicit args, append `ordenHistorialService` as the last argument matching the primary constructor's new last parameter.

- [ ] **Step 4: Add mock to OrdenServiceCreateScopeTest**

In `OrdenServiceCreateScopeTest.java`, add the import:

```java
import com.veloservice.ordenes.application.usecase.OrdenHistorialService;
```

Add the mock field alongside the other `@Mock` declarations:

```java
    @Mock private OrdenHistorialService ordenHistorialService;
```

Find where the test constructs `new OrdenService(...)` (in `@BeforeEach` / setup) and append `ordenHistorialService` as the final constructor argument, matching the new primary-constructor parameter position.

- [ ] **Step 5: Verify compile + existing tests still pass**

Run: `./mvnw -q -Dtest=OrdenServiceCreateScopeTest test`
Expected: PASS — wiring compiles, no behavior change yet.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceCreateScopeTest.java
git commit -m "feat(ordenes): wire OrdenHistorialService into OrdenService"
```

---

## Task 7: Hook estado change (TDD)

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Test: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java` (create)

- [ ] **Step 1: Write the failing test**

This test file mirrors the mock setup of `OrdenServiceCreateScopeTest`. To avoid duplicating ~25 mocks, construct `OrdenService` via the same secondary constructor the existing test uses. Copy the `@Mock` field block and `@BeforeEach` construction from `OrdenServiceCreateScopeTest` as the starting point, then add `@Mock private OrdenHistorialService ordenHistorialService;`.

```java
package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.domain.AccionHistorialEnum;
import com.veloservice.ordenes.domain.model.EstadoOrden;
import com.veloservice.ordenes.domain.model.Orden;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrdenServiceHistorialTest {

    // NOTE: replicate the @Mock field block + @BeforeEach OrdenService construction from
    // OrdenServiceCreateScopeTest, adding the ordenHistorialService mock below.
    @Mock private OrdenHistorialService ordenHistorialService;

    // ... other @Mock fields copied from OrdenServiceCreateScopeTest ...

    private OrdenService ordenService; // constructed in @BeforeEach like OrdenServiceCreateScopeTest

    @AfterEach
    void clearContext() {
        TallerContext.clear();
        SucursalContext.clear();
        UsuarioContext.clear();
    }

    @Test
    void cambiarEstado_recordsEstadoCambiadoEvent() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID estadoAnteriorId = UUID.randomUUID();
        UUID estadoNuevoId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(UUID.randomUUID());

        Orden orden = Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId)
                .estadoId(estadoAnteriorId).build();
        EstadoOrden anterior = EstadoOrden.builder().id(estadoAnteriorId).codigo("recibida").esFinal(false).build();
        EstadoOrden nuevo = EstadoOrden.builder().id(estadoNuevoId).codigo("en_diagnostico").esFinal(false).build();

        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(estadoOrdenRepository.findById(estadoAnteriorId)).willReturn(Optional.of(anterior));
        given(estadoOrdenRepository.findByCodigo("en_diagnostico")).willReturn(Optional.of(nuevo));

        OrdenEstadoChangeCommand cmd = new OrdenEstadoChangeCommand();
        cmd.setCodigo("en_diagnostico");
        ordenService.cambiarEstado(ordenId.toString(), cmd);

        verify(ordenHistorialService).registrar(
                eq(ordenId), eq(AccionHistorialEnum.ESTADO_CAMBIADO), eq("orden"), isNull(), any());
    }
}
```

> Adjust `given(...)` stubs and `OrdenEstadoChangeCommand` construction to match the real field names/setters in the codebase (`getCodigo`/`getObservacion`). Verify the exact `findByIdAndSucursalId` vs `findByNumeroOrden...` path used by `buscarOrdenParaMutacion` for a UUID id.

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest#cambiarEstado_recordsEstadoCambiadoEvent test`
Expected: FAIL — `registrar` never invoked.

- [ ] **Step 3: Add the hook in `aplicarCambioEstado`**

In `aplicarCambioEstado(...)`, after the `ordenEstadoRepository.save(OrdenEstado.builder()...)` block, add:

```java
        ordenHistorialService.registrar(
                orden.getId(),
                AccionHistorialEnum.ESTADO_CAMBIADO,
                "orden",
                null,
                java.util.Map.of(
                        "estadoAnterior", estadoAnterior.map(EstadoOrden::getCodigo).orElse("desconocido"),
                        "estadoNuevo", nuevoEstado.getCodigo(),
                        "observacion", observacion == null ? "" : observacion
                ));
```

Add the import at the top of `OrdenService.java`:

```java
import com.veloservice.ordenes.domain.AccionHistorialEnum;
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest#cambiarEstado_recordsEstadoCambiadoEvent test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java
git commit -m "feat(ordenes): record ESTADO_CAMBIADO in historial"
```

---

## Task 8: Hook product mutations (TDD)

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Test: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java`

- [ ] **Step 1: Write the failing test (product add)**

Add to `OrdenServiceHistorialTest`:

```java
    @Test
    void agregarProductos_recordsProductoAgregadoPerLineItem() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(UUID.randomUUID());

        Orden orden = Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId)
                .estadoId(UUID.randomUUID()).build();
        // stub buscarOrdenParaMutacion + validarOrdenPermiteModificarProductos preconditions,
        // buscarProductoDisponible(productoId), stock checks, and saveAll to return a line item.
        // Mirror the equivalent setup already present in OrdenServiceCreateScopeTest's product tests.

        // ... given(...) stubs ...

        OrdenProductoAddCommand item = new OrdenProductoAddCommand();
        item.setProductoId(productoId);
        item.setCantidad(2);
        ordenService.agregarProductos(ordenId.toString(), java.util.List.of(item));

        verify(ordenHistorialService).registrar(
                eq(ordenId), eq(AccionHistorialEnum.PRODUCTO_AGREGADO), eq("producto"), eq(productoId), any());
    }
```

> Reuse the exact product-path stubbing already written in `OrdenServiceCreateScopeTest` (look for its `agregarProductos` test) so the precondition mocks match reality.

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest#agregarProductos_recordsProductoAgregadoPerLineItem test`
Expected: FAIL.

- [ ] **Step 3: Add hooks in the three product helpers**

In `agregarProductosAOrden(...)`, inside the `for (OrdenProductoAddCommand item : items)` loop, after `lineItem.setProporcionadoPorCliente(proporcionadoPorCliente);` and any notes handling (right before the stock decrement block), capture the product and quantity; then **after** the `saveAll` near the end of the method, record per product. Simplest correct placement — record inside the loop using the `producto` and `item` in scope:

```java
            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.PRODUCTO_AGREGADO,
                    "producto",
                    item.getProductoId(),
                    java.util.Map.of(
                            "nombre", producto.getNombre(),
                            "cantidad", item.getCantidad(),
                            "proporcionadoPorCliente", proporcionadoPorCliente
                    ));
```

In `actualizarProductosAOrden(...)`, inside the `.map(item -> {...})` lambda, before `return lineItem;`, add:

```java
                    ordenHistorialService.registrar(
                            orden.getId(),
                            AccionHistorialEnum.PRODUCTO_MODIFICADO,
                            "producto",
                            lineItem.getProductoId(),
                            java.util.Map.of(
                                    "lineItemId", item.getId().toString(),
                                    "cantidad", cantidad,
                                    "proporcionadoPorCliente", proporcionadoPorCliente
                            ));
```

In `eliminarProductosAOrden(...)`, inside the `for` loop after the successful `deleted` check (`if (deleted == 0) {...}`), add:

```java
            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.PRODUCTO_QUITADO,
                    "producto",
                    null,
                    java.util.Map.of("lineItemId", lineItemId.toString()));
```

> `verify(producto.getNombre())` — confirm `Producto` has a `getNombre()` getter (it does, per the inventory model). Adjust the field name if different.

- [ ] **Step 4: Run the product tests to verify they pass**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java
git commit -m "feat(ordenes): record producto agregado/modificado/quitado in historial"
```

---

## Task 9: Hook service mutations (TDD)

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Test: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java`

- [ ] **Step 1: Write the failing test (service add)**

Add to `OrdenServiceHistorialTest` (mirror the service-path stubbing from `OrdenServiceCreateScopeTest`'s `agregarServicios` test):

```java
    @Test
    void agregarServicios_recordsServicioAgregadoPerLineItem() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(UUID.randomUUID());

        Orden orden = Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId)
                .estadoId(UUID.randomUUID()).build();
        // ... given(...) stubs for buscarOrdenParaMutacion, validarOrdenPermiteModificarServicios,
        //     buscarServicioDisponible, resolverPrecioServicio, saveAll ...

        OrdenServicioAddCommand item = new OrdenServicioAddCommand();
        item.setServicioId(servicioId);
        ordenService.agregarServicios(ordenId.toString(), java.util.List.of(item));

        verify(ordenHistorialService).registrar(
                eq(ordenId), eq(AccionHistorialEnum.SERVICIO_AGREGADO), eq("servicio"), eq(servicioId), any());
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest#agregarServicios_recordsServicioAgregadoPerLineItem test`
Expected: FAIL.

- [ ] **Step 3: Add hooks in the three service helpers**

In `agregarServiciosAOrden(...)`, inside the `.map(item -> {...})` lambda, before `return OrdenServicio.builder()...`, add (uses `servicio` already in scope):

```java
                    ordenHistorialService.registrar(
                            orden.getId(),
                            AccionHistorialEnum.SERVICIO_AGREGADO,
                            "servicio",
                            item.getServicioId(),
                            java.util.Map.of(
                                    "nombre", servicio.getNombre(),
                                    "precioBase", precioBase.toPlainString()
                            ));
```

In `actualizarServiciosAOrden(...)`, inside the `.map(item -> {...})` lambda, before `return lineItem;`, add:

```java
                    ordenHistorialService.registrar(
                            orden.getId(),
                            AccionHistorialEnum.SERVICIO_MODIFICADO,
                            "servicio",
                            lineItem.getServicioId(),
                            java.util.Map.of(
                                    "lineItemId", item.getId().toString(),
                                    "precioAplicado", lineItem.getPrecioAplicado().toPlainString(),
                                    "descuentoAplicado", lineItem.getDescuentoAplicado().toPlainString()
                            ));
```

In `eliminarServiciosAOrden(...)`, inside the `for` loop after the `if (deleted == 0) {...}` guard, add:

```java
            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.SERVICIO_QUITADO,
                    "servicio",
                    null,
                    java.util.Map.of("lineItemId", lineItemId.toString()));
```

> Confirm `Servicio` has `getNombre()`; adjust if the field differs.

- [ ] **Step 4: Run the service tests to verify they pass**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java
git commit -m "feat(ordenes): record servicio agregado/modificado/quitado in historial"
```

---

## Task 10: Hook multimedia add (TDD)

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Test: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java`

- [ ] **Step 1: Write the failing test (confirmarMultimedia path)**

Add to `OrdenServiceHistorialTest`. Mirror the multimedia-confirm stubbing from `OrdenServiceCreateScopeTest` if present; otherwise stub `multimediaRepository.save(...)` to return a saved `Multimedia` with an id and `multimediaRepository.findResultById(...)` to return a `MultimediaResult`.

```java
    @Test
    void confirmarMultimedia_recordsMultimediaAgregadaEvent() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID multimediaId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(UUID.randomUUID());

        // ... given(...) stubs to reach the multimediaRepository.save(...) call and return id=multimediaId ...

        // ordenService.confirmarMultimedia(ordenId.toString(), <confirm command/args>);

        verify(ordenHistorialService).registrar(
                eq(ordenId), eq(AccionHistorialEnum.MULTIMEDIA_AGREGADA), eq("multimedia"),
                eq(multimediaId), any());
    }
```

> Inspect the real `confirmarMultimedia` and `agregarMultimedia` signatures (line ~412 and ~462) to fill in the exact call arguments and stubs.

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest#confirmarMultimedia_recordsMultimediaAgregadaEvent test`
Expected: FAIL.

- [ ] **Step 3: Add hooks in both multimedia methods**

In `agregarMultimedia(...)`, after `Multimedia saved = multimediaRepository.save(...)` and before/after building the result (while `saved` is in scope, inside the try block before returning), add:

```java
            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.MULTIMEDIA_AGREGADA,
                    "multimedia",
                    saved.getId(),
                    java.util.Map.of(
                            "tipoArchivo", saved.getTipoArchivo() == null ? "" : saved.getTipoArchivo(),
                            "etapa", saved.getEtapa() == null ? "" : saved.getEtapa()
                    ));
```

> `orden` may be referenced by a local variable or by `saved.getOrdenId()` depending on scope. Use `saved.getOrdenId()` for the first argument if the `Orden` entity is not in scope at that point.

In `confirmarMultimedia(...)`, after its `Multimedia saved = multimediaRepository.save(...)` call (line ~something near 462-516), add the equivalent:

```java
        ordenHistorialService.registrar(
                saved.getOrdenId(),
                AccionHistorialEnum.MULTIMEDIA_AGREGADA,
                "multimedia",
                saved.getId(),
                java.util.Map.of(
                        "tipoArchivo", saved.getTipoArchivo() == null ? "" : saved.getTipoArchivo(),
                        "etapa", saved.getEtapa() == null ? "" : saved.getEtapa()
                ));
```

- [ ] **Step 4: Run the multimedia test to verify it passes**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java
git commit -m "feat(ordenes): record MULTIMEDIA_AGREGADA in historial"
```

---

## Task 11: Hook order-field edits — ORDEN_EDITADA (TDD)

> Records a single event when scalar order fields change in `actualizar()` (tipo, prioridad, mecanico, and any other direct field setters). Estado/product/service changes are already covered by their own events, so ORDEN_EDITADA captures only the remaining scalar edits.

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Test: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java`

- [ ] **Step 1: Write the failing test**

```java
    @Test
    void actualizar_recordsOrdenEditadaWhenScalarFieldChanges() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID nuevoMecanicoId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(UUID.randomUUID());

        Orden orden = Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId)
                .estadoId(UUID.randomUUID()).build();
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(usuarioRepository.existsActiveMecanicoById(nuevoMecanicoId)).willReturn(true);
        // stub obtenerDetalle(...) read-back to a minimal OrdenDetalleResult, mirroring existing tests.

        OrdenUpdateCommand cmd = new OrdenUpdateCommand();
        cmd.setMecanicoId(nuevoMecanicoId);
        ordenService.actualizar(ordenId.toString(), cmd);

        verify(ordenHistorialService).registrar(
                eq(ordenId), eq(AccionHistorialEnum.ORDEN_EDITADA), eq("orden"), isNull(), any());
    }
```

> The existing `actualizar` read-back calls `obtenerDetalle(id)`. Reuse the stubbing pattern from `OrdenServiceCreateScopeTest`'s `actualizar` test so the read-back returns a valid result.

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest#actualizar_recordsOrdenEditadaWhenScalarFieldChanges test`
Expected: FAIL.

- [ ] **Step 3: Add the hook in `actualizar(...)`**

Introduce a `Map<String,Object> camposEditados = new java.util.LinkedHashMap<>();` near the top of `actualizar` (after `boolean changed = false;`). In each scalar branch (tipo, prioridad, mecanico — **not** estado, products, services), add a `camposEditados.put(...)` recording the new value. Example for the mecanico branch:

```java
        if (command.getMecanicoId() != null) {
            if (!usuarioRepository.existsActiveMecanicoById(command.getMecanicoId())) {
                throw new ResourceNotFoundException("Mecanico no encontrado: " + command.getMecanicoId());
            }
            orden.setMecanicoId(command.getMecanicoId());
            camposEditados.put("mecanicoId", command.getMecanicoId().toString());
            changed = true;
        }
```

Add `camposEditados.put("tipoCodigo", command.getTipoCodigo());` in the tipo branch and `camposEditados.put("prioridad", prioridad);` in the prioridad branch (after the value is validated/normalized).

Then, in the final `if (changed) {...}` block, after `ordenRepository.save(orden);`, add:

```java
            if (!camposEditados.isEmpty()) {
                ordenHistorialService.registrar(
                        orden.getId(),
                        AccionHistorialEnum.ORDEN_EDITADA,
                        "orden",
                        null,
                        camposEditados);
            }
```

> Rationale for `camposEditados` (not `changed` alone): `changed` is also `true` for product/service/estado edits, which already have their own events. Recording ORDEN_EDITADA only when a scalar field actually changed avoids empty/duplicate audit noise.

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw -q -Dtest=OrdenServiceHistorialTest test`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceHistorialTest.java
git commit -m "feat(ordenes): record ORDEN_EDITADA for scalar field edits"
```

---

## Task 12: Expose history — GET /ordenes/{id}/historial (TDD)

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/interfaces/rest/dto/OrdenHistorialResponse.java`
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java` (add `listarHistorial`)
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java` (add endpoint)

- [ ] **Step 1: Create the response record**

```java
package com.veloservice.ordenes.interfaces.rest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenHistorialResponse(
        UUID id,
        String accion,
        String entidad,
        UUID entidadId,
        String detalle,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
) {}
```

- [ ] **Step 2: Add `listarHistorial` to OrdenService**

Resolve the order within the current tenant scope (reuse `buscarOrdenParaMutacion` to enforce visibility — it returns the `Orden` and throws if not in scope), then query the repository. Add the `OrdenHistorialRepository` as a dependency (field + both constructors + the two test files), OR reuse `OrdenHistorialService` by adding a read method to it. **Chosen approach:** add a read method to `OrdenHistorialService` to keep `OrdenService`'s dependency list unchanged beyond Task 6.

In `OrdenHistorialService`, add:

```java
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<com.veloservice.ordenes.application.dto.OrdenHistorialResult> listar(UUID ordenId) {
        return repository.findResultByOrdenId(ordenId);
    }
```

In `OrdenService`, add:

```java
    @Transactional(readOnly = true)
    public List<OrdenHistorialResult> listarHistorial(String id) {
        Orden orden = buscarOrdenParaMutacion(id);
        return ordenHistorialService.listar(orden.getId());
    }
```

Add the import `import com.veloservice.ordenes.application.dto.OrdenHistorialResult;` to `OrdenService.java`.

> Note: `buscarOrdenParaMutacion` is a read of the entity guarded by tenant scope; it does not mutate. Acceptable for a read-only endpoint.

- [ ] **Step 3: Add the controller endpoint**

In `OrdenController`, add imports:

```java
import com.veloservice.ordenes.application.dto.OrdenHistorialResult;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenHistorialResponse;
```

Add the endpoint method (near the detalle `GET` handler):

```java
    @GetMapping("/{id}/historial")
    public ResponseEntity<List<OrdenHistorialResponse>> historial(@PathVariable String id) {
        validarIdentificador(id);
        List<OrdenHistorialResult> results = ordenService.listarHistorial(id);
        List<OrdenHistorialResponse> response = results.stream()
                .map(h -> new OrdenHistorialResponse(
                        h.id(), h.accion(), h.entidad(), h.entidadId(),
                        h.detalle(), h.usuarioId(), h.usuario(), h.createdAt()))
                .toList();
        return ResponseEntity.ok(response);
    }
```

> Confirm `validarIdentificador` exists as a private helper in the controller (it is used by other endpoints). If it has a different name, match the existing id-validation pattern.

- [ ] **Step 4: Verify full build + test suite compiles and passes**

Run: `./mvnw -q test`
Expected: PASS — full suite green.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/interfaces/rest/dto/OrdenHistorialResponse.java \
        src/main/java/com/veloservice/ordenes/application/usecase/OrdenHistorialService.java \
        src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java
git commit -m "feat(ordenes): expose GET /ordenes/{id}/historial"
```

---

## Task 13: Manual end-to-end verification

**Files:** none (verification only).

- [ ] **Step 1: Boot the app against a real Postgres and confirm V7 applied**

Run the app (`./mvnw spring-boot:run` with the dev profile/DB) and verify Flyway logs show `V7__create_orden_historial` applied, and `\d orden_historial` shows the `jsonb detalle` column.

- [ ] **Step 2: Exercise each mutation and read back the timeline**

With a valid JWT (mecanico/recepcionista/jefe_taller/admin_taller):
1. `PATCH /ordenes/{id}/estado` → change estado.
2. `POST /ordenes/{id}/productos` → add a product.
3. `POST /ordenes/{id}/servicios` → add a service.
4. `PATCH /ordenes/{id}` → change mecanico/prioridad, and add+remove a product line in one call.
5. `POST /ordenes/{id}/multimedia` → upload a file.
6. `GET /ordenes/{id}/historial` → confirm one event per action, each with `usuario`, `accion`, and a populated `detalle` JSON, ordered newest-first.

Expected: every mutation produced exactly one corresponding history row; estado/product/service edits via the unified PATCH each produced their specific events (not duplicated by ORDEN_EDITADA).

- [ ] **Step 3: Confirm rollback semantics**

Trigger a mutation that fails after a partial change (e.g. add two products where the second is out of stock) and confirm **no** `orden_historial` rows were written for that failed request (same-transaction rollback).

---

## Self-Review Notes

- **Spec coverage:** estado ✅(T7), producto add/update/remove ✅(T8), servicio add/update/remove ✅(T9), order edit ✅(T11), multimedia add ✅(T10), expose ✅(T12). Multimedia **remove** has no endpoint today — documented as future hook (`MULTIMEDIA_QUITADA` enum value reserved).
- **Decision left to implementer/user:** whether to also surface `historial` inside `OrdenDetalleResult` (already carries `historialEstados`). Default: separate endpoint only, to keep detalle light. Change if the mobile/web client prefers one round-trip.
- **Risk:** `OrdenService` constructor churn (Task 6) ripples to `OrdenServiceCreateScopeTest`; the new `OrdenServiceHistorialTest` duplicates that mock block. If this duplication is painful, extract a shared `@BeforeEach` builder helper — but only after tests are green (YAGNI until then).
- **Type consistency:** `registrar(UUID, AccionHistorialEnum, String, UUID, Map<String,Object>)` signature is identical across all call sites and tests.
