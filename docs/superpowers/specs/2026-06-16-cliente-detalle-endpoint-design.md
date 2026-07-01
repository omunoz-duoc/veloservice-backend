# GET /clientes/{id} — Cliente Detalle Endpoint

**Date:** 2026-06-16
**Status:** Approved design, ready for implementation plan

## Goal

Enrich `GET /clientes/{id}` so it returns a full customer profile: identity fields,
the customer's bicycles, and a summary of their work orders (OTs). This replaces the
current thin `ClienteResponse` payload.

## Response Contract

`GET /clientes/{id}` returns:

```json
{
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "telefono": "+56912345678",
  "direccion": "Av. Siempre Viva 123",
  "rut": "12345678-9",
  "clienteDesde": "2026-01-15T10:30:00-03:00",
  "bicicletasCount": 2,
  "bicicletas": [
    {
      "id": "uuid",
      "marca": "Trek",
      "modelo": "Marlin 5",
      "tipo": "MTB",
      "aro": "29",
      "color": "Rojo",
      "numeroSerie": "ABC123",
      "anio": 2023,
      "notas": "..."
    }
  ],
  "otsCount": 7,
  "lastOts": [
    {
      "numeroOrden": "OT-0001",
      "tipoOrden": "Mantención",
      "estadoOrden": "En proceso",
      "fechaIngreso": "2026-06-10T09:00:00-03:00"
    }
  ]
}
```

### Field rules

| Field | Source |
|-------|--------|
| `nombre` | `cliente.nombre` + " " + `cliente.apellido`, trimmed (single combined field) |
| `email` | `cliente.email` |
| `telefono` | `cliente.telefono` |
| `direccion` | `cliente.direccion` (JSON key without accent — matches codebase convention; spec's "dirección" mapped to `direccion`) |
| `rut` | `cliente.rut` |
| `clienteDesde` | `cliente.createdAt` (`OffsetDateTime`) |
| `bicicletasCount` | size of `cliente.bicicletas` |
| `bicicletas[]` | each bike: `id, marca, modelo, tipo, aro, color, numeroSerie, anio, notas` |
| `otsCount` | count of **all** OTs across the client's bicycles, tenant-scoped by `tallerId` |
| `lastOts[]` | **5** most recent OTs by `fechaIngreso desc` |
| `lastOts[].tipoOrden` | `tipos_orden.nombre` (string name, not id/codigo) |
| `lastOts[].estadoOrden` | `estados_orden.nombre` (string name, not id/codigo) |
| `lastOts[].fechaIngreso` | `orden.fechaIngreso` |

## Architecture

Layers follow existing hexagonal structure (domain / application / infraestructure / interfaces).
Customer data and order data live in separate bounded contexts (`clientes`, `ordenes`).
The `ordenes` context already depends on `clientes` (its read queries join `Bicicleta`/`Cliente`).
Here we add the reverse dependency: `ClienteService` reads order aggregates via `OrdenRepository`.

### New DTOs — `clientes/interfaces/rest/dto`

- **`ClienteDetalleResponse`** — top-level response. Fields per contract above.
- **`BicicletaDetalleItem`** — record: `id, marca, modelo, tipo, aro, color, numeroSerie, anio, notas`.
  - Existing `BicicletaListItem` is **not** reused: it lacks `aro` and `notas`, and names the
    serial `numSerie` instead of `numeroSerie`.
- **`OrdenResumenItem`** — record: `numeroOrden, tipoOrden, estadoOrden, fechaIngreso`.

### New application results — `clientes/application/dto`

- **`ClienteDetalleResult`** — service-layer result mirroring the response, carrying domain types
  (`OffsetDateTime`, `UUID`).
- Nested bike + OT result records (or reuse a shared shape) carrying the aggregated data.

### Order query result — `ordenes/application/dto`

- **`OrdenResumenClienteResult`** — JPQL constructor-expression projection:
  `numeroOrden`, `tipo.nombre`, `estado.nombre`, `fechaIngreso`.

### OrdenRepository — 2 new queries

Both join `Bicicleta b on b.id = o.bicicletaId` then filter `b.clienteId = :clienteId`
and `o.tallerId = :tallerId` for tenant isolation.

```java
@Query("""
    select count(o)
    from Orden o
    join com.veloservice.clientes.domain.model.Bicicleta b on b.id = o.bicicletaId
    where b.clienteId = :clienteId
      and o.tallerId = :tallerId
    """)
long countByClienteIdAndTallerId(@Param("clienteId") UUID clienteId,
                                 @Param("tallerId") UUID tallerId);

@Query("""
    select new com.veloservice.ordenes.application.dto.OrdenResumenClienteResult(
        o.numeroOrden, t.nombre, e.nombre, o.fechaIngreso)
    from Orden o
    join com.veloservice.ordenes.domain.model.TipoOrden t on t.id = o.tipoId
    join com.veloservice.ordenes.domain.model.EstadoOrden e on e.id = o.estadoId
    join com.veloservice.clientes.domain.model.Bicicleta b on b.id = o.bicicletaId
    where b.clienteId = :clienteId
      and o.tallerId = :tallerId
    order by o.fechaIngreso desc
    """)
List<OrdenResumenClienteResult> findResumenByClienteIdAndTallerId(
        @Param("clienteId") UUID clienteId,
        @Param("tallerId") UUID tallerId,
        Pageable pageable);
```

Caller passes `Pageable.ofSize(5)` to limit `lastOts` to 5.

### ClienteService — new `obtenerDetalle`

```java
@TenantOperation
@Transactional(readOnly = true)
public ClienteDetalleResult obtenerDetalle(UUID id)
```

Steps:
1. Resolve `tallerId` from `TallerContext`; throw `IllegalStateException` if null (matches `obtener`).
2. `clienteRepository.findByIdAndTallerId(id, tallerId)` → 404-style `IllegalArgumentException("Cliente no encontrado")` if absent.
3. Map `cliente.bicicletas` → bike items; `bicicletasCount` = size.
4. `otsCount` = `ordenRepository.countByClienteIdAndTallerId(id, tallerId)`.
5. `lastOts` = `ordenRepository.findResumenByClienteIdAndTallerId(id, tallerId, Pageable.ofSize(5))`.
6. Build `ClienteDetalleResult`.

Inject `OrdenRepository` into `ClienteService` via constructor (Lombok `@RequiredArgsConstructor`).
The existing `obtener(UUID)` and its stubbed `ordenesCount`/`totalGastado` are left untouched
(out of scope; still used by the mapper/other callers). Implementation plan confirms no other
caller depends on `GET /{id}` returning the old `ClienteResponse`.

### ClienteMapper — new `toDetalleResponse`

`ClienteDetalleResult` → `ClienteDetalleResponse`. Combines `nombre`+`apellido` here (or in service —
plan decides; keep consistent with existing `buildNombreCompleto`).

### ClienteController — `GET /{id}`

```java
@GetMapping("/{id}")
public ResponseEntity<ClienteDetalleResponse> obtener(@PathVariable UUID id) {
    return ResponseEntity.ok(
        ClienteMapper.toDetalleResponse(clienteService.obtenerDetalle(id)));
}
```

## Error Handling

- Missing customer → `IllegalArgumentException("Cliente no encontrado")` (existing pattern; mapped by global handler).
- Missing tenant context → `IllegalStateException` (existing pattern).
- Client with zero bikes → `bicicletas: []`, `bicicletasCount: 0`, `otsCount: 0`, `lastOts: []`.

## Testing

- **`ClienteServiceTest`**: `obtenerDetalle` happy path (bikes + OTs aggregated), zero-bike client,
  not-found throws, tenant-context-null throws. Mock `OrdenRepository`.
- **`OrdenRepository`** (data-layer test): `countByClienteIdAndTallerId` and
  `findResumenByClienteIdAndTallerId` return correct rows, respect `tallerId` isolation, order desc, limit 5.
- **Controller/mapper**: `toDetalleResponse` field mapping, `nombre` concatenation, JSON keys.

## Out of Scope

- Implementing the stubbed `ordenesCount`/`totalGastado` in `obtener`/`ClienteResult`.
- `GET /buscar` and `GET /resumen` stubs.
- Pagination of `lastOts` beyond fixed 5.
