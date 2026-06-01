# Orden Resumen Endpoint Design

**Date:** 2026-06-01
**Branch:** feat/contrato-api

## Goal

Add `GET /ordenes/resumen` — a lightweight list endpoint returning only the fields needed for a summary view of work orders.

## Context

- `OrdenService.listar()` already executes a single JPQL query (READ_SELECT) joining 6 tables: Orden, EstadoOrden, TipoOrden, Bicicleta, Cliente, Usuario (mecanico).
- `OrdenReadResult` already contains all required fields.
- `OrdenResumenResult` (application layer DTO) is stale/unused — will be deleted.
- `OrdenResumenResponse` (REST DTO) exists but has wrong shape — will be updated.

## Endpoint

```
GET /ordenes/resumen
Auth: same as GET /ordenes (roles: mecanico, recepcionista, jefe_taller, admin_taller)
```

### Response

```json
{
  "total": 2,
  "ordenes": [
    {
      "numeroOrden": "OT-2026-001",
      "tipo": "Reparación",
      "fechaIngreso": "2026-05-30T10:00:00Z",
      "mecanico": "Juan Pérez",
      "cliente": "María González",
      "bicicleta": {
        "marca": "Trek",
        "modelo": "FX3",
        "tipo": "Urbana",
        "color": "Rojo"
      },
      "diagnosticoInicial": "Frenos desgastados",
      "estado": "En diagnóstico"
    }
  ]
}
```

### Field rules

| Field | Source in OrdenReadResult | Notes |
|---|---|---|
| `numeroOrden` | `numeroOrden` | |
| `tipo` | `tipoNombre` | Human-readable nombre |
| `fechaIngreso` | `fechaIngreso` | |
| `mecanico` | `mecanicoNombre + " " + mecanicoApellido` | `"No asignado"` when mecanicoId is null |
| `cliente` | `clienteNombre + " " + clienteApellido` | |
| `bicicleta.marca` | `bicicletaMarca` | |
| `bicicleta.modelo` | `bicicletaModelo` | |
| `bicicleta.tipo` | `bicicletaTipo` | |
| `bicicleta.color` | `bicicletaColor` | |
| `diagnosticoInicial` | `diagnosticoInicial` | Can be null |
| `estado` | `estadoNombre` | Human-readable nombre |

## Implementation Plan

### Files to change

1. **Delete** `OrdenResumenResult.java` — stale, never used
2. **Update** `OrdenResumenResponse.java` — new record shape matching the response above
3. **Update** `OrdenController.java` — add `GET /ordenes/resumen` endpoint with inline mapping method `toResumenResponse(OrdenReadResult)`

### Mapping

```java
// in OrdenController
@GetMapping("/resumen")
public ResponseEntity<OrdenResumenListResponse> listarResumen() {
    List<OrdenReadResult> results = ordenService.listar();
    List<OrdenResumenResponse> resumen = results.stream()
        .map(this::toResumenResponse)
        .toList();
    return ResponseEntity.ok(new OrdenResumenListResponse(resumen.size(), resumen));
}

private OrdenResumenResponse toResumenResponse(OrdenReadResult r) {
    String mecanico = r.mecanicoId() != null
        ? r.mecanicoNombre() + " " + r.mecanicoApellido()
        : "No asignado";
    return new OrdenResumenResponse(
        r.numeroOrden(),
        r.tipoNombre(),
        r.fechaIngreso(),
        mecanico,
        r.clienteNombre() + " " + r.clienteApellido(),
        new OrdenResumenResponse.BicicletaResumenResponse(
            r.bicicletaMarca(), r.bicicletaModelo(), r.bicicletaTipo(), r.bicicletaColor()
        ),
        r.diagnosticoInicial(),
        r.estadoNombre()
    );
}
```

### DTOs to update

**`OrdenResumenResponse`** (interfaces/rest/dto/):
```java
public record OrdenResumenResponse(
    String numeroOrden,
    String tipo,
    OffsetDateTime fechaIngreso,
    String mecanico,
    String cliente,
    BicicletaResumenResponse bicicleta,
    String diagnosticoInicial,
    String estado
) {
    public record BicicletaResumenResponse(
        String marca, String modelo, String tipo, String color
    ) {}
}
```

New wrapper record needed:
**`OrdenResumenListResponse`** (interfaces/rest/dto/):
```java
public record OrdenResumenListResponse(int total, List<OrdenResumenResponse> ordenes) {}
```

## What does NOT change

- `OrdenReadResult` — unchanged
- `OrdenService.listar()` — unchanged (reused)
- `OrdenRepository` — unchanged (reused)
- `GET /ordenes` — unchanged
