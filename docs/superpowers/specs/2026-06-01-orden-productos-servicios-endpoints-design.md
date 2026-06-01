# Orden Productos & Servicios Endpoints Design

**Date:** 2026-06-01
**Branch:** feat/contrato-api

## Goal

Add two POST endpoints that append line items to an existing work order:

- `POST /ordenes/{id}/productos` — adds one or more product line items to `orden_productos`
- `POST /ordenes/{id}/servicios` — adds one or more service line items to `orden_servicios`

Both capture price snapshots at insert time (same pattern as `OrdenService.crear()`).

## Context

- `OrdenProducto` entity: `id`, `ordenId`, `productoId`, `cantidad`, `precioCostoSnapshot`, `precioVentaSnapshot`, `precioAplicado`, `proporcionadoPorCliente` (default `false`), `notas`, `createdAt`.
- `OrdenServicio` entity: `id`, `ordenId`, `servicioId`, `precioBaseSnapshot`, `precioAplicado`, `descuentoAplicado`, `notas`, `createdAt`.
- `Producto` is scoped to `sucursalId`; `Servicio` is scoped to `tallerId`. Tenant validation must use the correct scope for each.
- `sucursal_servicios` overrides exist per-sucursal for custom pricing — the servicios price lookup must check for a sucursal override before falling back to `servicio.precioBase`.
- Neither `orden_productos` nor `orden_servicios` has a UNIQUE constraint on `(orden_id, producto_id/servicio_id)` — duplicate calls append new rows.
- Both `OrdenProductoRepository` and `OrdenServicioRepository` already exist but contain broken JPQL queries (SQL table names instead of FQN entity class names, SQL column names instead of Java field names). These must be fixed as part of this implementation.
- `OrdenProductoResult` and `OrdenServicioResult` exist but have bugs that prevent JPQL constructor expressions from working (see Implementation Notes below).

## Endpoints

```
POST /ordenes/{id}/productos
POST /ordenes/{id}/servicios

Auth: same as existing orden endpoints (roles: mecanico, recepcionista, jefe_taller, admin_taller)
Tenant: TenantContext (tallerId + sucursalId)
```

`{id}` is the internal UUID of the order.

---

### POST /ordenes/{id}/productos

#### Request body

```json
[
  {
    "productoId": "uuid",
    "cantidad": 2,
    "proporcionadoPorCliente": false,
    "notas": "string or null"
  }
]
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `productoId` | UUID | yes | Must exist in `productos` and belong to `orden.sucursalId` |
| `cantidad` | Integer | yes | `>= 1` |
| `proporcionadoPorCliente` | Boolean | no | Defaults to `false` |
| `notas` | String | no | Free text |

#### Response — 201 Created

```json
[
  {
    "id": "uuid",
    "productoId": "uuid",
    "nombre": "Pastillas de freno Shimano",
    "sku": "SKU-001",
    "cantidad": 2,
    "precioVenta": 12500.00
  }
]
```

`precioVenta` in the response is the snapshotted `precioVentaSnapshot` value stored at insert time.

---

### POST /ordenes/{id}/servicios

#### Request body

```json
[
  {
    "servicioId": "uuid",
    "notas": "string or null"
  }
]
```

| Field | Type | Required | Notes |
|---|---|---|---|
| `servicioId` | UUID | yes | Must exist in `servicios` and belong to `orden.tallerId` |
| `notas` | String | no | Free text |

#### Response — 201 Created

```json
[
  {
    "id": "uuid",
    "servicioId": "uuid",
    "nombre": "Ajuste de frenos",
    "precioBase": 8500.00
  }
]
```

`precioBase` in the response is the snapshotted `precioBaseSnapshot` value stored at insert time.

---

## Validation & Error Cases

| Case | HTTP | Message |
|---|---|---|
| Order not found | 404 | `"Orden no encontrada"` |
| Order belongs to different taller | 403 | `"Acceso denegado"` (TenantContext check) |
| `productoId` not found | 404 | `"Producto no encontrado: <id>"` |
| Producto belongs to different `sucursalId` | 400 | `"Producto <id> no pertenece a la sucursal de esta orden"` |
| `servicioId` not found | 404 | `"Servicio no encontrado: <id>"` |
| Servicio belongs to different `tallerId` | 400 | `"Servicio <id> no pertenece al taller de esta orden"` |
| `cantidad < 1` | 400 | Bean validation (`@Min(1)`) |
| Empty list body | 400 | Bean validation (`@NotEmpty`) |

---

## Price Snapshot Logic

### Productos

```
precioCostoSnapshot = producto.precioCosto
precioVentaSnapshot = producto.precioVenta
precioAplicado      = precioVentaSnapshot
proporcionadoPorCliente = item.proporcionadoPorCliente (default false)
```

### Servicios

```
1. Look up SucursalServicio for (servicioId, orden.sucursalId)
2. If found and precio_personalizado != null → precioBaseSnapshot = sucursalServicio.precioPersonalizado
3. Else → precioBaseSnapshot = servicio.precioBase
precioAplicado   = precioBaseSnapshot
descuentoAplicado = 0.00
```

---

## Implementation

### Files to FIX (existing bugs must be resolved)

**`OrdenProductoResult.java`** — convert to Java record:

```java
public record OrdenProductoResult(
    UUID id, UUID productoId, String nombre, String sku,
    Integer cantidad, BigDecimal precioVenta
) {}
```

> Current `@Data @Builder` without `@AllArgsConstructor` breaks JPQL `SELECT new` constructor expressions. Record generates a canonical constructor automatically.

**`OrdenServicioResult.java`** — convert to Java record, fix `precioBase` type:

```java
public record OrdenServicioResult(
    UUID id, UUID servicioId, String nombre, BigDecimal precioBase
) {}
```

> Current `precioBase` is `String` — should be `BigDecimal` to match `Servicio.precioBase`.

**`OrdenProductoRepository.java`** — fix JPQL (see pattern in `ComentarioRepository` for correct syntax):

```java
@Query("""
    SELECT new com.veloservice.ordenes.application.dto.OrdenProductoResult(
        op.id, op.productoId, p.nombre, p.sku, op.cantidad, op.precioVentaSnapshot
    )
    FROM OrdenProducto op
    JOIN com.veloservice.inventario.domain.model.Producto p ON p.id = op.productoId
    WHERE op.ordenId = :ordenId
    """)
List<OrdenProductoResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
```

**`OrdenServicioRepository.java`** — fix JPQL:

```java
@Query("""
    SELECT new com.veloservice.ordenes.application.dto.OrdenServicioResult(
        os.id, os.servicioId, s.nombre, os.precioBaseSnapshot
    )
    FROM OrdenServicio os
    JOIN com.veloservice.servicios.domain.model.Servicio s ON s.id = os.servicioId
    WHERE os.ordenId = :ordenId
    """)
List<OrdenServicioResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
```

### Files to UPDATE (existing, need changes)

**`OrdenProductoAddRequest.java`** — remove `ordenId` field (comes from path param), add `@NotEmpty` on the list wrapper:

New shape (single item — the controller accepts `List<OrdenProductoAddRequest>`):
```java
public class OrdenProductoAddRequest {
    @NotNull UUID productoId;
    @NotNull @Min(1) Integer cantidad;
    Boolean proporcionadoPorCliente = false;
    String notas;
}
```

**`OrdenServicioAddRequest.java`** — same, remove `ordenId`:

```java
public class OrdenServicioAddRequest {
    @NotNull UUID servicioId;
    String notas;
}
```

### Files to CREATE (new)

**`OrdenServicioResponse.java`** (`interfaces/rest/dto/`) — REST response DTO for a service line item:

```java
public record OrdenServicioResponse(
    UUID id, UUID servicioId, String nombre, BigDecimal precioBase
) {}
```

> `OrdenProductoResponse` already exists with the right shape — no change needed there.

### Service layer — `OrdenService.java`

Add two new methods (both `@Transactional`):

**`agregarProductos(UUID ordenId, List<OrdenProductoAddCommand> items)`**:
```
1. Load Orden by ordenId — verify tallerId matches TenantContext, else 403
2. For each item:
   a. Load Producto by productoId — verify producto.sucursalId == orden.sucursalId, else 400
   b. Build OrdenProducto:
      - ordenId, productoId, cantidad, proporcionadoPorCliente, notas
      - precioCostoSnapshot = producto.precioCosto
      - precioVentaSnapshot = producto.precioVenta
      - precioAplicado = precioVentaSnapshot
      - createdAt = OffsetDateTime.now()
3. saveAll(lineItems)
4. Return List<OrdenProductoResult> (query via findResultByOrdenId for the new IDs)
```

**`agregarServicios(UUID ordenId, List<OrdenServicioAddCommand> items)`**:
```
1. Load Orden by ordenId — verify tallerId matches TenantContext, else 403
2. For each item:
   a. Load Servicio by servicioId — verify servicio.tallerId == orden.tallerId, else 400
   b. Resolve price: check SucursalServicio for (servicioId, orden.sucursalId);
      use precioPersonalizado if found, else servicio.precioBase
   c. Build OrdenServicio:
      - ordenId, servicioId, notas
      - precioBaseSnapshot = resolved price
      - precioAplicado = precioBaseSnapshot
      - descuentoAplicado = BigDecimal.ZERO
      - createdAt = OffsetDateTime.now()
3. saveAll(lineItems)
4. Return List<OrdenServicioResult> (query via findResultByOrdenId for the new IDs)
```

### Controller — `OrdenController.java`

Add two handlers:

```java
@PostMapping("/{id}/productos")
@ResponseStatus(HttpStatus.CREATED)
public List<OrdenProductoResponse> agregarProductos(
    @PathVariable UUID id,
    @RequestBody @Valid @NotEmpty List<OrdenProductoAddRequest> items
) {
    var commands = items.stream().map(r -> toProductoCommand(r)).toList();
    return ordenService.agregarProductos(id, commands)
        .stream().map(r -> toProductoResponse(r)).toList();
}

@PostMapping("/{id}/servicios")
@ResponseStatus(HttpStatus.CREATED)
public List<OrdenServicioResponse> agregarServicios(
    @PathVariable UUID id,
    @RequestBody @Valid @NotEmpty List<OrdenServicioAddRequest> items
) {
    var commands = items.stream().map(r -> toServicioCommand(r)).toList();
    return ordenService.agregarServicios(id, commands)
        .stream().map(r -> toServicioResponse(r)).toList();
}
```

Private mappers `toProductoCommand`, `toServicioCommand`, `toProductoResponse`, `toServicioResponse` follow same pattern as existing `toCommand()` in the controller.

---

## What Does NOT Change

- `OrdenService.crear()` — unchanged
- `OrdenCreateCommand` / `NuevaOrdenRequest` — unchanged
- `OrdenReadResult` / `OrdenDetalleResult` — `productos`/`servicios` lists in `OrdenDetalleResult` will now correctly populate once the JPQL bugs are fixed (no structural change needed to those records)
- `GET /ordenes/{id}` — unchanged (the JPQL fix above is a prerequisite for products/services to appear in the detail response, but no spec change required)
- `OrdenProductoResponse` — already has the correct shape, no change needed

---

## Open Question

**SucursalServicio lookup**: if a `SucursalServicioRepository` or similar does not exist, use `servicio.precioBase` directly as `precioBaseSnapshot` and document the override lookup as a follow-up. Do not add a new repository just for this spec — confirm the repository exists before including it in the implementation.
