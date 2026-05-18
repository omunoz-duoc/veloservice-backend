# Design: Missing API Endpoints

Date: 2026-05-17  
Branch: feat/ordenes-por-mecanico

## Scope

Implement all endpoints defined in `docs/api-endpoints.md` that are currently missing or misconfigured in the backend.

---

## Gap Summary

| Endpoint | Status | Fix |
|---|---|---|
| `GET /ordenes/estados` | ⚠️ Wrong output | Map 8 internal states → 4 groups |
| `PATCH /ordenes/:id/estado` | ⚠️ Method | Add `@PatchMapping` alias alongside `PUT` |
| `GET /ordenes/:id/comentarios` | ❌ Missing | Full vertical slice (migration + entity + service + endpoint) |
| `POST /ordenes/:id/comentarios` | ❌ Missing | Same vertical slice |
| `GET /ordenes/:id/multimedia` | ⚠️ Wrong path | Add endpoints in `OrdenController` delegating to existing `MultimediaService` |
| `POST /ordenes/:id/multimedia` | ⚠️ Wrong path | Same — keep JSON `{url, tipoArchivo, descripcion}` body |
| `DELETE /ordenes/:id/multimedia/:mediaId` | ❌ Missing | Add to new multimedia endpoints in `OrdenController` |
| `GET /ordenes/:id/productos` | ❌ Missing | Add endpoint, enrich `OrdenProducto` with product name/sku |
| `DELETE /ordenes/:id/productos/:productoId` | ❌ Missing | Add repo method + restore stock + endpoint |
| `GET /productos?search=<query>` | ❌ Missing | Add optional `?search=` param with JPQL text search |

---

## 1. `GET /ordenes/estados` — 4-group mapping

The endpoint currently streams raw `EstadoOrdenEnum` names into a map. The mobile app expects exactly 4 keys.

**Mapping:**
```
recibida             → recibida
en_diagnostico       → en_proceso
esperando_repuestos  → en_proceso
en_reparacion        → en_proceso
control_calidad      → en_proceso
lista_para_entrega   → lista_para_entrega
entregada            → entregada
cancelada            → (omitted / not counted)
```

**Change:** Replace the `Collectors.groupingBy(o -> o.getEstado().name())` in `OrdenController.estados()` with a helper that maps each `EstadoOrdenEnum` to one of the 4 group keys.

---

## 2. `PATCH /ordenes/:id/estado`

No logic change needed. Add `@PatchMapping("/{id}/estado")` alongside the existing `@PutMapping` annotation on `OrdenController.cambiarEstado()`.

---

## 3. Comentarios — new vertical slice

### 3a. Migration `V6__create_comentarios.sql`

```sql
CREATE TABLE comentarios (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id   UUID NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    texto      TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comentarios_orden ON comentarios(orden_id);
```

### 3b. `Comentario` entity

Package: `com.veloservice.ordenes.domain.model`  
Table: `comentarios`  
Fields: `id (UUID)`, `ordenId (UUID)`, `usuarioId (UUID)`, `texto (String)`, `createdAt (OffsetDateTime)`

### 3c. `ComentarioRepository`

Package: `com.veloservice.ordenes.infraestructure.persistence.repository`  
Extends `JpaRepository<Comentario, UUID>`  
Method: `List<Comentario> findByOrdenIdOrderByCreatedAtAsc(UUID ordenId)`

### 3d. `ComentarioService`

Package: `com.veloservice.ordenes.application.usecase`  
Methods:
- `List<ComentarioResult> listarPorOrden(UUID ordenId)`
- `ComentarioResult agregar(UUID ordenId, String texto)` — reads `UsuarioContext.getCurrentUser()` for `usuarioId`

`ComentarioResult` DTO: `id`, `autor` (resolved from `usuarios` table via `UsuarioRepository` or passed as display string), `texto`, `creadoEn`

> **Note on `autor` field:** The API spec returns `"autor": "Juan Pérez"` (full name string). The service must resolve the user's `nombre + apellido` from `UsuarioRepository` by `usuarioId`.

### 3e. Request/Response DTOs (in `interfaces/rest`)

`ComentarioRequest`: `@NotBlank String texto`  
`ComentarioResponse`: `String id`, `String autor`, `String texto`, `OffsetDateTime creadoEn`

### 3f. Endpoints in `OrdenController`

```
GET  /ordenes/{id}/comentarios  → ComentarioService.listarPorOrden(id)
POST /ordenes/{id}/comentarios  → ComentarioService.agregar(id, request.texto)
```

---

## 4. Multimedia — path fix + DELETE

The existing `MultimediaService` is correct. `MultimediaController` stays as-is (backwards compatibility). Add new endpoints **in `OrdenController`** that delegate to `MultimediaService`.

### 4a. `GET /ordenes/{id}/multimedia`

Returns `List<MultimediaResponse>` — same shape as current `MultimediaController.listarPorOrden()`.

Response per item: `{ "id": "...", "url": "..." }` (API spec only requires these two fields; existing `MultimediaResponse` is a superset, which is fine).

### 4b. `POST /ordenes/{id}/multimedia`

Body: JSON `{ "url": "...", "tipoArchivo": "...", "descripcion": "..." }` (URL approach confirmed — mobile handles upload externally).  
Delegates to existing `MultimediaService.subir()`.

Note: API doc says `multipart/form-data` but URL approach is confirmed by user. The API doc will remain as-is; the mobile app is the consumer and is aware.

### 4c. `DELETE /ordenes/{id}/multimedia/{mediaId}`

Calls `multimediaRepository.deleteById(mediaId)`. Returns `200 {}`.  
`MultimediaRepository` already extends `JpaRepository` so `deleteById` is available.

Inject `MultimediaRepository` directly into `OrdenController` for this (or add a `delete(UUID id)` method to `MultimediaService`).

---

## 5. `GET /ordenes/:id/productos`

`OrdenProductoRepository.findByOrdenId()` already exists. Need to enrich the response with product `nombre`, `sku`, `precioVenta` from the `productos` table.

**Response shape per item:**
```json
{
  "id": "<orden_producto.id>",
  "productoId": "<producto.id>",
  "nombre": "<producto.nombre>",
  "sku": "<producto.sku>",
  "cantidad": 1,
  "precioVenta": 18900
}
```

**Implementation:**
- `OrdenService` or new `OrdenProductoService.listarPorOrden(UUID ordenId)` — fetches `OrdenProducto` list, batch-fetches products from `ProductoRepository.findAllById()`, assembles response.
- New `OrdenProductoResponse` DTO with the 6 fields above.
- New endpoint in `OrdenController`: `GET /ordenes/{id}/productos`

---

## 6. `DELETE /ordenes/:id/productos/:productoId`

The path param `:productoId` is the **catalog product id** (not the `orden_producto.id`), per the API spec.

**Steps:**
1. Add `Optional<OrdenProducto> findByOrdenIdAndProductoId(UUID ordenId, UUID productoId)` to `OrdenProductoRepository`.
2. In service: find the `OrdenProducto`, restore stock (`producto.stock += cantidad`), delete the record.
3. Endpoint in `OrdenController`: `DELETE /ordenes/{id}/productos/{productoId}` → `200 {}`

---

## 7. `GET /productos?search=<query>`

**Repository:** Add to `ProductoRepository`:
```java
@Query("SELECT p FROM Producto p WHERE p.sucursalId = :sucursalId AND " +
       "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
       " LOWER(p.sku)    LIKE LOWER(CONCAT('%', :q, '%')))")
List<Producto> searchBySucursalId(@Param("sucursalId") UUID sucursalId, @Param("q") String q);
```

**Service:** `ProductoService.buscar(String query)` — if `query` blank, delegates to `listar()`; else calls `searchBySucursalId`.

**Controller:** Add `@RequestParam(required = false) String search` to `ProductoController.listar()`. If non-null and ≥ 1 char, call `buscar(search)`; else call `listar()`.

---

## Files to Create

| File | Type |
|---|---|
| `db/migration/V6__create_comentarios.sql` | Migration |
| `ordenes/domain/model/Comentario.java` | Entity |
| `ordenes/infraestructure/persistence/repository/ComentarioRepository.java` | Repository |
| `ordenes/application/dto/ComentarioResult.java` | DTO |
| `ordenes/application/usecase/ComentarioService.java` | Service |
| `ordenes/interfaces/rest/ComentarioRequest.java` | Request DTO |
| `ordenes/interfaces/rest/ComentarioResponse.java` | Response DTO |
| `ordenes/interfaces/rest/OrdenProductoResponse.java` | Response DTO |

## Files to Modify

| File | Change |
|---|---|
| `OrdenController.java` | Add comentario, multimedia, producto endpoints; fix estados mapping; add PATCH alias |
| `ProductoController.java` | Add `?search=` param |
| `ProductoService.java` | Add `buscar(String)` method |
| `ProductoRepository.java` | Add `searchBySucursalId` JPQL query |
| `OrdenProductoRepository.java` | Add `findByOrdenIdAndProductoId` |
| `OrdenService.java` | Add `listarProductosPorOrden` + `eliminarProducto` |
| `MultimediaService.java` | Add `eliminar(UUID id)` method (optional, can call repo directly) |

---

## Out of Scope

- No changes to `MultimediaController` (keep backwards compat)
- No multipart/file upload — URL approach confirmed
- No auth/permission changes
- No frontend changes
