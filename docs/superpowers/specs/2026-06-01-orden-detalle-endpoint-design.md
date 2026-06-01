# Orden Detalle Endpoint Design

**Date:** 2026-06-01
**Branch:** feat/contrato-api

## Goal

Replace the existing `GET /ordenes/{id}` with a richer response that includes order details, comments, and multimedia — all with user names resolved.

## Context

- Current `GET /ordenes/{id}` returns `OrdenReadResponse` (same shape as the list endpoint — no comments, no multimedia).
- `OrdenComentario` and `Multimedia` entities exist with bare UUID FK columns (`usuarioId`), no `@ManyToOne`.
- `ComentarioRepository.findByOrdenIdOrderByCreatedAtAsc()` and `MultimediaRepository.findByOrdenId()` exist but do NOT resolve user names.
- `Usuario` entity has `nombre` and `apellido`. Cross-bounded JPQL JOINs on UUID FK are already the established pattern (READ_SELECT in OrdenRepository does this for mecanico).
- The existing `READ_SELECT` JPQL query in OrdenRepository already fetches all scalar orden fields including estado, tipo, bicicleta, cliente, and mecanico in a single round-trip — reuse it.

## Endpoint

```
GET /ordenes/{id}
Auth: same as existing (roles: mecanico, recepcionista, jefe_taller, admin_taller)
```

`{id}` is the internal UUID of the order (existing behavior).

### Response shape

```json
{
  "id": "uuid",
  "numeroOrden": "OT-2026-001",
  "tallerId": "uuid",
  "sucursalId": "uuid",
  "estado": { "id": "uuid", "codigo": "en_diagnostico", "nombre": "En diagnóstico" },
  "tipo": { "id": "uuid", "codigo": "reparacion", "nombre": "Reparación" },
  "fechaIngreso": "2026-05-30T10:00:00Z",
  "fechaPrometida": "2026-06-02T10:00:00Z",
  "fechaEntrega": null,
  "diagnosticoInicial": "Frenos desgastados",
  "diagnosticoFinal": null,
  "observacionesCliente": "Urgente",
  "bicicleta": {
    "id": "uuid",
    "marca": "Trek",
    "modelo": "FX3",
    "tipo": "Urbana",
    "color": "Rojo",
    "numeroSerie": "SN-12345"
  },
  "cliente": {
    "id": "uuid",
    "nombre": "María",
    "apellido": "González",
    "telefono": "+56912345678",
    "email": "maria@gmail.com",
    "rut": "12345678-9"
  },
  "mecanico": {
    "id": "uuid",
    "nombre": "Juan",
    "apellido": "Pérez"
  },
  "comentarios": [
    {
      "usuario": "Juan Pérez",
      "texto": "Se revisaron los frenos traseros",
      "createdAt": "2026-05-30T11:00:00Z"
    }
  ],
  "multimedia": [
    {
      "usuario": "Juan Pérez",
      "tipoArchivo": "imagen",
      "url": "https://r2.example.com/...",
      "etapa": "diagnostico",
      "descripcion": "Foto del freno trasero"
    }
  ]
}
```

### Field notes

- `mecanico` is `null` when no mechanic is assigned.
- `comentarios` is ordered by `created_at ASC`.
- `multimedia.descripcion` is nullable.
- All scalar fields of `OrdenDetalleResult` come from the existing `READ_SELECT` JPQL (single DB round-trip). Comments and multimedia are two additional queries.

## Implementation

### New application-layer DTOs (`application/dto/`)

**`ComentarioResult.java`**
```java
public record ComentarioResult(String usuario, String texto, OffsetDateTime createdAt) {}
```

**`MultimediaResult.java`**
```java
public record MultimediaResult(
    String usuario,
    String tipoArchivo,
    String url,
    String etapa,
    String descripcion,
    OffsetDateTime createdAt
) {}
```

**`OrdenDetalleResult.java`**
```java
public record OrdenDetalleResult(
    UUID id,
    String numeroOrden,
    UUID tallerId,
    UUID sucursalId,
    UUID estadoId, String estadoCodigo, String estadoNombre,
    UUID tipoId, String tipoCodigo, String tipoNombre,
    OffsetDateTime fechaIngreso,
    OffsetDateTime fechaPrometida,
    OffsetDateTime fechaEntrega,
    String diagnosticoInicial,
    String diagnosticoFinal,
    String observacionesCliente,
    UUID bicicletaId, String bicicletaMarca, String bicicletaModelo,
    String bicicletaTipo, String bicicletaColor, String bicicletaNumeroSerie,
    UUID clienteId, String clienteNombre, String clienteApellido,
    String clienteTelefono, String clienteEmail, String clienteRut,
    UUID mecanicoId, String mecanicoNombre, String mecanicoApellido,
    List<ComentarioResult> comentarios,
    List<MultimediaResult> multimedia
) {}
```

`OrdenDetalleResult` is assembled in the service from three sources:
1. `OrdenReadResult` (from existing `READ_SELECT` JPQL) — all scalar fields
2. `List<ComentarioResult>` — new JPQL query with Usuario JOIN
3. `List<MultimediaResult>` — new JPQL query with Usuario JOIN

### New REST DTO (`interfaces/rest/dto/`)

**`OrdenDetalleResponse.java`**

Records mirroring `OrdenDetalleResult` structure plus nested records:
- `CatalogoResponse(UUID id, String codigo, String nombre)` — reuse existing from `OrdenReadResponse`
- `BicicletaDetalleResponse(UUID id, String marca, String modelo, String tipo, String color, String numeroSerie)`
- `ClienteDetalleResponse(UUID id, String nombre, String apellido, String telefono, String email, String rut)`
- `MecanicoDetalleResponse(UUID id, String nombre, String apellido)` — nullable
- `ComentarioResponse(String usuario, String texto, OffsetDateTime createdAt)`
- `MultimediaResponse(String usuario, String tipoArchivo, String url, String etapa, String descripcion)`

### New JPQL queries

**`ComentarioRepository`** — new method:
```java
@Query("""
    SELECT new com.veloservice.ordenes.application.dto.ComentarioResult(
        CONCAT(u.nombre, ' ', u.apellido), c.texto, c.createdAt
    )
    FROM OrdenComentario c
    JOIN Usuario u ON u.id = c.usuarioId
    WHERE c.ordenId = :ordenId
    ORDER BY c.createdAt ASC
    """)
List<ComentarioResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
```

**`MultimediaRepository`** — new method:
```java
@Query("""
    SELECT new com.veloservice.ordenes.application.dto.MultimediaResult(
        CONCAT(u.nombre, ' ', u.apellido),
        CAST(m.tipoArchivo AS string),
        m.url,
        CAST(m.etapa AS string),
        m.descripcion,
        m.createdAt
    )
    FROM Multimedia m
    JOIN Usuario u ON u.id = m.usuarioId
    WHERE m.ordenId = :ordenId
    ORDER BY m.createdAt ASC
    """)
List<MultimediaResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
```

> Note: `tipoArchivo` and `etapa` are enums. JPQL `CAST(... AS string)` may need adjustment to `.name()` or a String column approach depending on Hibernate version behavior. Validate during implementation.

### `OrdenService` changes

New method `obtenerDetalle(String id)`:
```
1. Resolve tallerId/sucursalId from TenantContext (same as existing obtener())
2. Query OrdenReadResult via existing READ_SELECT query
3. Query List<ComentarioResult> via ComentarioRepository.findResultByOrdenId(orden.getId())
4. Query List<MultimediaResult> via MultimediaRepository.findResultByOrdenId(orden.getId())
5. Assemble and return OrdenDetalleResult
```

Three DB queries total. No N+1.

### `OrdenController` changes

Update `obtener()` method:
- Call `ordenService.obtenerDetalle(id)` instead of `ordenService.obtener(id)`
- Map `OrdenDetalleResult` → `OrdenDetalleResponse` via new `toDetalleResponse()` inline method

## What does NOT change

- `OrdenReadResult` — unchanged (still used by `listar()` and the new resumen endpoint)
- `OrdenService.listar()` — unchanged
- `GET /ordenes/resumen` — unchanged
- `OrdenRepository` READ_SELECT — unchanged (reused for the scalar fields)
- `ComentarioRepository.findByOrdenIdOrderByCreatedAtAsc()` — kept (other potential callers)
- `MultimediaRepository.findByOrdenId()` — kept (other potential callers)
