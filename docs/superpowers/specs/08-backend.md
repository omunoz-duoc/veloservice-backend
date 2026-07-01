# Phase 8 - Mobile Backend API Contract

This document is the implementation contract for the current VeloService mechanic
mobile application. It was cross-checked against:

- `veloservice-mobile`: real services, domain types, mock data, and rendered fields.
- `veloservice-web` `develop`: auth and order clients, DTO assumptions, and pickers.
- `veloservice-backend` `develop`: Spring controllers, request DTOs, response DTOs,
  tenant contexts, security configuration, and the global exception handler.

The contract is intentionally backward compatible with existing web/backend field
names. Fields and routes marked **required change** are additions needed by mobile.

## 1. Base URL, formats, and authentication

- Base path: `/api/v1`.
- JSON media type: `application/json; charset=utf-8`.
- Timestamps: ISO 8601 with offset, for example `2026-06-08T14:30:00-04:00`.
- IDs: UUID strings unless a field explicitly says otherwise.
- Money: JSON numbers in CLP, with up to two decimal places. Clients format currency.
- Empty collections are `[]`, never `null`.
- Optional scalar/object fields may be `null` as specified below.

`POST /auth/login` and `POST /auth/reset-password` are public. Every other endpoint
requires:

```http
Authorization: Bearer <jwt>
```

The JWT is authoritative for `tallerId`, role, scope (`ambito`), and the user's
assigned `sucursalId`. A request `sucursalId` may only narrow the JWT-authorized
scope. It must never switch tenant or grant access to another branch.

- Taller-wide users may omit `sucursalId` or select a branch belonging to their
  `tallerId`.
- Branch-scoped users may omit `sucursalId` or send their own branch only.
- Cross-tenant resources must return `404` to avoid disclosing their existence.
- A branch outside the token's allowed scope returns `403`.
- A missing/invalid/expired JWT returns `401`.

### Order identifier rule

Every `{id}` under `/ordenes/{id}` must resolve either:

1. the internal order UUID, or
2. the tenant-unique `numeroOrden`, such as `OT-0343`.

Resolution always occurs inside the JWT tenant scope. This is a **required change**
for append endpoints, whose current Spring path variables accept UUID only.

## 2. Backend status matrix

Status refers to `veloservice-backend` `develop` as inspected on 2026-06-08.

| Method | Path | Status | Required work |
|---|---|---|---|
| `POST` | `/auth/login` | Exists | Keep compatible |
| `POST` | `/auth/logout` | Missing | Add stateless `204` |
| `POST` | `/auth/reset-password` | Exists | Wire mobile recovery action |
| `GET` | `/ordenes` | Exists | Add display fields and tenant-safe branch filter |
| `GET` | `/ordenes/metricas` | Missing | Add |
| `GET` | `/ordenes/{id}` | Exists | Add timeline timestamps/history and address |
| `PATCH` | `/ordenes/{id}/estado` | Exists | Add state-history metadata; accept both ID forms |
| `POST` | `/ordenes/{id}/comentarios` | Missing | Add |
| `POST` | `/ordenes/{id}/multimedia` | Missing | Add storage/upload flow |
| `GET` | `/servicios/sucursal` | Exists | Add service display metadata to picker response |
| `GET` | `/productos/lista-productos` | Exists | Keep compatible |
| `POST` | `/ordenes/{id}/servicios` | Exists | Accept both ID forms; expose timestamp/author |
| `POST` | `/ordenes/{id}/productos` | Exists | Accept both ID forms; expose timestamp/author |

`/ordenes/urgentes` and `/ordenes/{id}/eventos` are not required. Urgency is derived
from the list, and the timeline is built from detail arrays.

## 3. Common schemas

### 3.1 Error envelope

All non-2xx responses use the backend's standard envelope:

```json
{
  "timestamp": "2026-06-08T14:32:11.482-04:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validacion fallida",
  "errors": {
    "texto": "no debe estar vacio"
  }
}
```

| Field | Type | Null | Notes |
|---|---|---:|---|
| `timestamp` | ISO timestamp | No | Server time |
| `status` | integer | No | HTTP status |
| `error` | string | No | HTTP reason phrase |
| `message` | string | No | Safe user-facing summary |
| `errors` | object of string values | Yes | Present for field validation only |

Controllers must not return empty framework-generated error bodies. Relevant
statuses are `400`, `401`, `403`, `404`, `409`, `413`, `415`, `429`, and `500`.

### 3.2 Catalog and person summaries

```json
{
  "id": "ff740e13-4f0e-43fc-8838-e48e88a18393",
  "codigo": "en_reparacion",
  "nombre": "En reparacion"
}
```

All catalog fields are non-null strings. Person summaries use `id`, `nombre`, and
`apellido`; `mecanico` may be `null` when unassigned.

### 3.3 Order state codes

| Mobile phase | API `codigo` |
|---|---|
| `recibida` | `recibida` |
| `diagnostico` | `en_diagnostico` |
| `repuestos` | `esperando_repuestos` |
| `reparacion` | `en_reparacion` |
| `calidad` | `control_calidad` |
| `listo` | `lista_para_entrega` |
| delivery | `entregada` |

`cancelada` remains valid for web/backend compatibility.

### 3.4 Order summary

The object below is returned inside `GET /ordenes`. It retains all current fields
and adds the fields needed by active and past order cards.

```json
{
  "id": "c68ca628-360f-4a89-a466-b702db109d95",
  "numeroOrden": "OT-0343",
  "tallerId": "6620315d-71ab-41ec-b918-4706a3524242",
  "sucursalId": "2a8e7732-9ef4-49e4-b8f2-14e52a1def14",
  "estado": {
    "id": "ff740e13-4f0e-43fc-8838-e48e88a18393",
    "codigo": "en_reparacion",
    "nombre": "En reparacion"
  },
  "tipo": {
    "id": "80226840-9c76-4a39-a339-5d60fc8f9357",
    "codigo": "mantencion",
    "nombre": "Mantencion"
  },
  "fechaIngreso": "2026-06-08T09:30:00-04:00",
  "fechaPrometida": "2026-06-08T18:00:00-04:00",
  "fechaEntrega": null,
  "diagnosticoInicial": "Ruido en la transmision",
  "diagnosticoFinal": null,
  "observacionesCliente": "Llamar antes de cambiar el cassette",
  "prioridad": "urgente",
  "cliente": {
    "id": "50e00f83-ac91-432c-93e2-fbd00b89a0f8",
    "nombre": "Paulina",
    "apellido": "Mora",
    "telefono": "+56967321451",
    "email": "paulina@example.cl",
    "rut": "12345678-5",
    "direccion": "Providencia"
  },
  "bicicleta": {
    "id": "169349f3-0991-4934-a4ea-41893ca6a255",
    "marca": "Trek",
    "modelo": "Marlin 7 2024",
    "tipo": "MTB",
    "aro": "M",
    "color": "Rojo Volcan",
    "numeroSerie": "WTU123456"
  },
  "mecanico": {
    "id": "e652d208-aa46-41b6-bd73-b05c1919b84f",
    "nombre": "Rodrigo",
    "apellido": "Soto"
  },
  "servicioResumen": "Mantencion Premium",
  "montoTotal": 142500
}
```

Nullability and validation:

| Field | Null | Notes |
|---|---:|---|
| `id`, `numeroOrden`, tenant IDs, `estado`, `tipo`, `fechaIngreso` | No | Existing identity fields |
| `fechaPrometida`, `fechaEntrega` | Yes | Delivery card uses the available date |
| diagnosis/observation strings | Yes | Do not substitute display text in the API |
| `prioridad` | No | `baja`, `media`, `alta`, or `urgente` |
| `cliente` and its `id`, names | No | Contact fields and `direccion` may be null |
| `bicicleta` and its `id`, `marca`, `modelo` | No | Remaining bike fields may be null |
| `mecanico` | Yes | Unassigned order |
| `servicioResumen` | Yes | First/primary service name; null with no service |
| `montoTotal` | Yes | Required for delivered-order amount; null until priced |

`montoTotal` is the authoritative sum of applied service and product prices after
discounts. It is not sent as a formatted currency string.

### 3.5 Timeline records

The detail response preserves separate arrays. Each record contains enough metadata
for the mobile client to merge and sort a timeline oldest to newest.

```json
{
  "comentarios": [
    {
      "id": "42dd4393-33a6-43fa-b2db-c2b2e832202d",
      "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
      "usuario": "Rodrigo Soto",
      "texto": "Cassette con desgaste avanzado.",
      "createdAt": "2026-06-08T11:40:00-04:00"
    }
  ],
  "multimedia": [
    {
      "id": "5f7f210f-6920-485f-91a8-26ad2dca17b6",
      "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
      "usuario": "Rodrigo Soto",
      "tipoArchivo": "image/jpeg",
      "categoria": "imagen",
      "url": "https://storage.example/ordenes/c68ca628/foto-1.jpg",
      "etapa": "diagnostico",
      "descripcion": "Desgaste de transmision",
      "createdAt": "2026-06-08T11:05:00-04:00"
    }
  ],
  "productos": [
    {
      "id": "45306f2a-c348-4238-8798-8e9804b050bb",
      "productoId": "8eeea844-8041-49d6-b440-651b7c1185ac",
      "nombre": "Cadena Shimano CN-HG601",
      "sku": "SH-CN-HG601",
      "cantidad": 1,
      "precioVenta": 34990,
      "precioAplicado": 32990,
      "notas": null,
      "proporcionadoPorCliente": false,
      "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
      "usuario": "Rodrigo Soto",
      "createdAt": "2026-06-08T11:02:00-04:00"
    }
  ],
  "servicios": [
    {
      "id": "291d8df1-8cb0-4d5d-948f-a833988f00d7",
      "servicioId": "8292415a-ac24-4e65-b795-da60f1f5ad15",
      "nombre": "Mantencion Premium",
      "precioBase": 72000,
      "precioAplicado": 72000,
      "descuentoAplicado": 0,
      "notas": null,
      "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
      "usuario": "Rodrigo Soto",
      "createdAt": "2026-06-08T09:34:00-04:00"
    }
  ],
  "historialEstados": [
    {
      "id": "aa5c341d-445e-4c12-bffd-fffb421bd91b",
      "estadoAnterior": {
        "id": "fcbe6b4b-a7cc-44e2-ab97-b37ee92b6923",
        "codigo": "en_diagnostico",
        "nombre": "En diagnostico"
      },
      "estadoNuevo": {
        "id": "ff740e13-4f0e-43fc-8838-e48e88a18393",
        "codigo": "en_reparacion",
        "nombre": "En reparacion"
      },
      "observacion": "Diagnostico aprobado",
      "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
      "usuario": "Rodrigo Soto",
      "createdAt": "2026-06-08T10:10:00-04:00"
    }
  ]
}
```

For every record, `id`, `usuario`, and `createdAt` are non-null. `usuarioId` may be
null only for imported/system records. Notes, descriptions, stage, and state
observation may be null. Existing product/service price fields remain valid.

## 4. Authentication

### 4.1 `POST /auth/login` - exists

No bearer token.

Request:

```json
{
  "email": "r.soto@veloservice.cl",
  "password": "rodrigosoto"
}
```

| Field | Validation |
|---|---|
| `email` | Required, valid email, trimmed, max 254 characters |
| `password` | Required, non-blank, max 256 characters |

Success: `200 OK`

```json
{
  "nombre": "Rodrigo",
  "apellido": "Soto",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "rol": "mecanico",
  "ambito": "sucursal",
  "tallerId": "6620315d-71ab-41ec-b918-4706a3524242",
  "sucursalId": "2a8e7732-9ef4-49e4-b8f2-14e52a1def14"
}
```

All fields are non-null except `sucursalId`, which may be null for taller-wide
users. Errors: `400` invalid payload, `401` invalid credentials, `403` inactive or
invalidly scoped account, `429` too many attempts.

### 4.2 `POST /auth/logout` - missing

Requires bearer token. The current JWT design is stateless, so the endpoint is an
idempotent compatibility operation; clients still erase local auth state.

Request body: none. Success: `204 No Content`, including when called twice with the
same still-valid JWT. Error: `401` invalid/expired JWT.

If token revocation is introduced later, this route may add the presented token to
a deny list without changing the response contract.

### 4.3 `POST /auth/reset-password` - exists, mobile unwired

No bearer token.

```json
{
  "email": "r.soto@veloservice.cl"
}
```

`email` is required and valid. Success: `200 OK` with an empty body. The same
success is returned whether the account exists, preventing account enumeration.
Errors: `400` invalid email and `429` rate limit.

This endpoint requests a reset email only. Completing the reset remains the
existing web flow through `/auth/change-password`; it is outside the three-screen
mobile scope.

## 5. Orders

### 5.1 `GET /ordenes` - exists, response change required

Query:

| Parameter | Type | Required | Rules |
|---|---|---:|---|
| `sucursalId` | UUID | No | May only narrow JWT scope |

Success: `200 OK`

```json
{
  "total": 2,
  "ordenes": [
    {
      "id": "c68ca628-360f-4a89-a466-b702db109d95",
      "numeroOrden": "OT-0343",
      "tallerId": "6620315d-71ab-41ec-b918-4706a3524242",
      "sucursalId": "2a8e7732-9ef4-49e4-b8f2-14e52a1def14",
      "estado": {
        "id": "ff740e13-4f0e-43fc-8838-e48e88a18393",
        "codigo": "en_reparacion",
        "nombre": "En reparacion"
      },
      "tipo": {
        "id": "80226840-9c76-4a39-a339-5d60fc8f9357",
        "codigo": "mantencion",
        "nombre": "Mantencion"
      },
      "fechaIngreso": "2026-06-08T09:30:00-04:00",
      "fechaPrometida": "2026-06-08T18:00:00-04:00",
      "fechaEntrega": null,
      "diagnosticoInicial": "Ruido en la transmision",
      "diagnosticoFinal": null,
      "observacionesCliente": null,
      "prioridad": "urgente",
      "cliente": {
        "id": "50e00f83-ac91-432c-93e2-fbd00b89a0f8",
        "nombre": "Paulina",
        "apellido": "Mora",
        "telefono": "+56967321451",
        "email": "paulina@example.cl",
        "rut": "12345678-5",
        "direccion": "Providencia"
      },
      "bicicleta": {
        "id": "169349f3-0991-4934-a4ea-41893ca6a255",
        "marca": "Trek",
        "modelo": "Marlin 7 2024",
        "tipo": "MTB",
        "aro": "M",
        "color": "Rojo Volcan",
        "numeroSerie": "WTU123456"
      },
      "mecanico": {
        "id": "e652d208-aa46-41b6-bd73-b05c1919b84f",
        "nombre": "Rodrigo",
        "apellido": "Soto"
      },
      "servicioResumen": "Mantencion Premium",
      "montoTotal": 142500
    }
  ]
}
```

The item schema is defined in section 3.4. `total` is a non-negative integer and
must equal `ordenes.length` until pagination is introduced. Mobile separates active
from delivered/cancelled orders locally. Errors: `400` malformed branch UUID,
`401`, and `403` disallowed branch.

### 5.2 `GET /ordenes/metricas` - missing

Uses the same optional tenant-safe `sucursalId` query parameter as the list.

Success: `200 OK`

```json
{
  "recibidas": 3,
  "enProceso": 8,
  "listas": 2,
  "entregadas": 24
}
```

All values are non-null, non-negative integers:

- `recibidas`: current state `recibida`.
- `enProceso`: diagnosis, waiting for parts, repair, and quality-control states.
- `listas`: current state `lista_para_entrega`.
- `entregadas`: current state `entregada`.

The mobile `activas` count is `recibidas + enProceso + listas`. Urgent count is
derived from active list items whose priority is `alta` or `urgente`; it is not
duplicated here. Errors: `400`, `401`, and `403`.

### 5.3 `GET /ordenes/{id}` - exists, response change required

`id` follows the UUID-or-`numeroOrden` rule.

Success: `200 OK`. The response contains every order-summary field plus detailed
bike fields and all timeline arrays:

```json
{
  "id": "c68ca628-360f-4a89-a466-b702db109d95",
  "numeroOrden": "OT-0343",
  "tallerId": "6620315d-71ab-41ec-b918-4706a3524242",
  "sucursalId": "2a8e7732-9ef4-49e4-b8f2-14e52a1def14",
  "estado": {
    "id": "ff740e13-4f0e-43fc-8838-e48e88a18393",
    "codigo": "en_reparacion",
    "nombre": "En reparacion"
  },
  "tipo": {
    "id": "80226840-9c76-4a39-a339-5d60fc8f9357",
    "codigo": "mantencion",
    "nombre": "Mantencion"
  },
  "fechaIngreso": "2026-06-08T09:30:00-04:00",
  "fechaPrometida": "2026-06-08T18:00:00-04:00",
  "fechaEntrega": null,
  "diagnosticoInicial": "Ruido en la transmision",
  "diagnosticoFinal": null,
  "observacionesCliente": "Llamar antes de cambiar el cassette",
  "prioridad": "urgente",
  "cliente": {
    "id": "50e00f83-ac91-432c-93e2-fbd00b89a0f8",
    "nombre": "Paulina",
    "apellido": "Mora",
    "telefono": "+56967321451",
    "email": "paulina@example.cl",
    "rut": "12345678-5",
    "direccion": "Providencia"
  },
  "bicicleta": {
    "id": "169349f3-0991-4934-a4ea-41893ca6a255",
    "marca": "Trek",
    "modelo": "Marlin 7 2024",
    "tipo": "MTB",
    "aro": "M",
    "color": "Rojo Volcan",
    "numeroSerie": "WTU123456",
    "anio": 2024,
    "fotoUrl": null,
    "notas": null
  },
  "mecanico": {
    "id": "e652d208-aa46-41b6-bd73-b05c1919b84f",
    "nombre": "Rodrigo",
    "apellido": "Soto"
  },
  "servicioResumen": "Mantencion Premium",
  "montoTotal": 142500,
  "comentarios": [],
  "multimedia": [],
  "productos": [],
  "servicios": [],
  "historialEstados": []
}
```

Array record schemas and nullability are defined in section 3.5. `fotoUrl` and
`notas` may be null; `anio` may be null when unknown. Preserve the existing arrays
even when empty. Required response additions are:

- `cliente.direccion`.
- `servicioResumen` and `montoTotal`.
- `multimedia[].id`, `usuarioId`, `categoria`, and `createdAt`.
- `productos[].usuarioId`, `usuario`, and `createdAt`.
- `servicios[].usuarioId`, `usuario`, and `createdAt`.
- `historialEstados`.

Errors: `400` malformed identifier, `401`, and `404` unknown or out-of-tenant order.

### 5.4 `PATCH /ordenes/{id}/estado` - exists

`id` follows the UUID-or-`numeroOrden` rule.

```json
{
  "codigo": "lista_para_entrega",
  "observacion": "Prueba final completada"
}
```

| Field | Validation |
|---|---|
| `codigo` | Required, one of the state codes in section 3.3 or `cancelada` |
| `observacion` | Optional/null, trimmed, max 1000 characters |

Success: `200 OK` with the complete order-detail response from section 5.3. The
server derives actor and timestamp and appends one `historialEstados` record. A
transition to `entregada` sets `fechaEntrega` to server time if not already set.

Errors: `400` invalid state/transition, `401`, `403` role cannot change state,
`404`, and `409` concurrent or already-finalized transition.

### 5.5 `POST /ordenes/{id}/comentarios` - missing

`id` follows the UUID-or-`numeroOrden` rule.

```json
{
  "texto": "Cassette con desgaste avanzado; esperando aprobacion."
}
```

`texto` is required, trimmed, 1-4000 characters after trimming. Author, author ID,
and timestamp come from the authenticated user and server.

Success: `201 Created`

```json
{
  "id": "42dd4393-33a6-43fa-b2db-c2b2e832202d",
  "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
  "usuario": "Rodrigo Soto",
  "texto": "Cassette con desgaste avanzado; esperando aprobacion.",
  "createdAt": "2026-06-08T11:40:00-04:00"
}
```

All response fields are non-null. Errors: `400` validation, `401`, `403` role,
and `404` order.

### 5.6 `POST /ordenes/{id}/multimedia` - missing

`id` follows the UUID-or-`numeroOrden` rule. Content type is
`multipart/form-data`; do not send a JSON body.

| Part | Type | Required | Validation |
|---|---|---:|---|
| `file` | binary | Yes | Non-empty; extension is not trusted |
| `tipoArchivo` | string | Yes | Exact MIME type and must match detected content |
| `descripcion` | string | No | Trimmed, max 500 characters |
| `etapa` | string | No | `recepcion`, `diagnostico`, `reparacion`, `calidad`, `entrega` |

Allowed MIME mapping:

| MIME | Domain `categoria` | Max size |
|---|---|---:|
| `image/jpeg`, `image/png`, `image/webp` | `imagen` | 10 MiB |
| `video/mp4`, `video/quicktime` | `video` | 100 MiB |
| `application/pdf` | `documento` | 20 MiB |

The server must inspect file signatures, generate a non-user-controlled storage
key, and reject MIME spoofing. Stored files use private tenant-scoped storage.
`url` is an absolute HTTPS download URL (or authorized signed URL) and must never
contain a local filesystem path.

Success: `201 Created`

```json
{
  "id": "5f7f210f-6920-485f-91a8-26ad2dca17b6",
  "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
  "usuario": "Rodrigo Soto",
  "tipoArchivo": "image/jpeg",
  "categoria": "imagen",
  "url": "https://storage.example/ordenes/c68ca628/foto-1.jpg",
  "etapa": "diagnostico",
  "descripcion": "Desgaste de transmision",
  "createdAt": "2026-06-08T11:05:00-04:00"
}
```

`etapa` and `descripcion` may be null; other fields are non-null. Errors: `400`
invalid metadata/MIME mismatch, `401`, `403`, `404`, `413` too large, `415`
unsupported media, and `500`/`503` storage failure without a persisted orphan row.

## 6. Service and product pickers

These existing endpoints are required before wiring the wrench and product quick
actions currently shown in the detail screen.

### 6.1 `GET /servicios/sucursal` - exists, response change required

Optional `sucursalId` follows the tenant rule. The current backend only returns
link IDs and price. Mobile requires additive catalog display fields.

Success: `200 OK`

```json
[
  {
    "id": "4600dab2-aad8-49a7-acd0-d8a9ffb9872d",
    "sucursalId": "2a8e7732-9ef4-49e4-b8f2-14e52a1def14",
    "servicioId": "8292415a-ac24-4e65-b795-da60f1f5ad15",
    "nombre": "Mantencion Premium",
    "descripcion": "Ajuste completo y lubricacion",
    "precioBase": 72000,
    "precioPersonalizado": null,
    "precioVigente": 72000,
    "activo": true,
    "createdAt": "2026-01-10T09:00:00-03:00"
  }
]
```

`descripcion` and `precioPersonalizado` may be null. Other fields are non-null.
`precioVigente` equals `precioPersonalizado` when present, otherwise `precioBase`.
Only active services available to the selected branch are returned. Errors: `400`,
`401`, and `403`.

### 6.2 `GET /productos/lista-productos` - exists

Optional `sucursalId` follows the tenant rule.

Success: `200 OK`

```json
{
  "productos": [
    {
      "id": "8eeea844-8041-49d6-b440-651b7c1185ac",
      "nombre": "Cadena Shimano CN-HG601",
      "precioVenta": 34990,
      "stock": 8
    }
  ]
}
```

All fields are non-null. `stock` is a non-negative integer and `precioVenta` is
non-negative. Only products available to the selected branch are returned.
Errors: `400` when no branch can be resolved, `401`, and `403`.

## 7. Add services and products

Requests are arrays to preserve the current Spring contract and allow a picker to
submit several selections atomically. The catalog UUID identifies the selected
item. Prices in responses are snapshots: later catalog price changes must not
rewrite existing order lines.

### 7.1 `POST /ordenes/{id}/servicios` - exists

`id` follows the UUID-or-`numeroOrden` rule.

```json
[
  {
    "servicioId": "8292415a-ac24-4e65-b795-da60f1f5ad15",
    "notas": "Incluir limpieza de transmision"
  }
]
```

The array must contain 1-50 items. `servicioId` is a required UUID referencing an
active service available to the order branch. `notas` is optional/null, trimmed,
max 1000 characters. Duplicate service IDs in one request return `400`.

Success: `201 Created`

```json
[
  {
    "id": "291d8df1-8cb0-4d5d-948f-a833988f00d7",
    "servicioId": "8292415a-ac24-4e65-b795-da60f1f5ad15",
    "nombre": "Mantencion Premium",
    "precioBase": 72000,
    "precioAplicado": 72000,
    "descuentoAplicado": 0,
    "notas": "Incluir limpieza de transmision",
    "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
    "usuario": "Rodrigo Soto",
    "createdAt": "2026-06-08T09:34:00-04:00"
  }
]
```

`notas` may be null; all other fields are non-null. The operation is atomic.
Errors: `400` validation/unavailable service, `401`, `403`, `404` order or catalog
item hidden by tenant scope, and `409` duplicate line if duplicates are forbidden.

### 7.2 `POST /ordenes/{id}/productos` - exists

`id` follows the UUID-or-`numeroOrden` rule.

```json
[
  {
    "productoId": "8eeea844-8041-49d6-b440-651b7c1185ac",
    "cantidad": 1,
    "proporcionadoPorCliente": false,
    "notas": null
  }
]
```

The array must contain 1-50 items.

| Field | Validation |
|---|---|
| `productoId` | Required UUID available in order branch |
| `cantidad` | Required integer, 1-9999 |
| `proporcionadoPorCliente` | Optional; defaults to `false` |
| `notas` | Optional/null, trimmed, max 1000 characters |

For workshop-provided products, the server validates and decrements branch stock in
the same transaction. Customer-provided products do not consume stock. Duplicate
product IDs in one request return `400`.

Success: `201 Created`

```json
[
  {
    "id": "45306f2a-c348-4238-8798-8e9804b050bb",
    "productoId": "8eeea844-8041-49d6-b440-651b7c1185ac",
    "nombre": "Cadena Shimano CN-HG601",
    "sku": "SH-CN-HG601",
    "cantidad": 1,
    "precioVenta": 34990,
    "precioAplicado": 34990,
    "notas": null,
    "proporcionadoPorCliente": false,
    "usuarioId": "e652d208-aa46-41b6-bd73-b05c1919b84f",
    "usuario": "Rodrigo Soto",
    "createdAt": "2026-06-08T11:02:00-04:00"
  }
]
```

`sku` and `notas` may be null; other fields are non-null. The operation is atomic.
Errors: `400` validation, `401`, `403`, `404`, and `409` insufficient stock or a
concurrent stock conflict. A stock error message identifies the product and
available quantity without exposing another branch's inventory.

## 8. Deferred contracts

These are not backend requirements for the current three-screen release. Mobile
contains notification stubs only, and the corresponding navigation destinations
are placeholders.

### 8.1 Device registration

Proposed future endpoint:

```http
POST /api/v1/devices
Authorization: Bearer <jwt>
Content-Type: application/json
```

```json
{
  "token": "ExponentPushToken[xxxxxxxxxxxxxxxxxxxxxx]",
  "plataforma": "ios",
  "deviceId": "optional-installation-id"
}
```

`plataforma` is `ios` or `android`. The endpoint should upsert by user/device and
return `204 No Content`. Token removal/rotation and push provider details remain to
be designed before implementation.

### 8.2 Notification inbox

Proposed future operations:

```text
GET   /api/v1/notificaciones
PATCH /api/v1/notificaciones/{id}/leida
```

Pagination, unread counts, retention, and event payload schemas are not yet defined.
The backend's current order-notification controller is not a mobile inbox contract.

### 8.3 Placeholder UI

No new backend work is required yet for:

- Order search.
- Push notification bell/badge.
- Board.
- Messages.
- Profile.

## 9. Gap analysis and implementation order

### Missing backend endpoints

1. `POST /auth/logout`.
2. `GET /ordenes/metricas`.
3. `POST /ordenes/{id}/comentarios`.
4. `POST /ordenes/{id}/multimedia`.

### Existing endpoints requiring compatibility changes

1. Order list/detail: add customer address, primary service summary, and total.
2. Order detail: expose multimedia, product, and service timestamps/actors.
3. Order detail/state update: expose state-transition history.
4. All order `{id}` routes: accept UUID or `numeroOrden`; product/service append
   currently bind UUID only.
5. Branch service picker: include service name, description, base price, and
   effective price.
6. Ensure all branch filters narrow JWT scope instead of trusting client context.

### Existing but unwired mobile capabilities

- Password recovery visibly links to the existing reset request endpoint.
- Wrench and product quick actions need the existing catalog and append endpoints
  wired into mobile sheets.

### Corrections to the previous document

- Logout does not exist on backend `develop`.
- Order metrics do not exist on backend `develop`.
- Product and service append endpoints already exist and return `201`.
- `/ordenes/urgentes` does not exist on backend `develop`.
- The current service branch response is not sufficient to render a picker.

Recommended backend sequence: shared identifier/tenant resolver, additive order
DTO fields, metrics, comments, media storage, then picker metadata. This minimizes
duplicate authorization and lookup logic across new routes.

## 10. Acceptance scenarios

1. **Login:** valid mechanic credentials return `200` and a scoped JWT; invalid
   credentials return the standard `401` envelope.
2. **Logout:** an authenticated request returns `204`; the mobile clears its token
   and subsequent protected calls return `401`.
3. **Recovery:** a valid email request returns `200` for known and unknown accounts;
   repeated abuse returns `429`.
4. **Tenant isolation:** a branch mechanic cannot list, load, mutate, or infer an
   order from another branch or tenant by changing `sucursalId` or `{id}`.
5. **List loading:** `GET /ordenes` supplies every card field, including address,
   promised/delivery date, mechanic, bike data, priority, service summary, and
   delivered-order amount.
6. **Metrics:** counts match states in the same tenant scope; urgent count remains
   derivable from the list.
7. **Detail loading:** UUID and `numeroOrden` return the same order. Arrays are
   always present and timestamps reproduce the demo timeline ordering.
8. **State change:** a valid transition returns updated detail and one new history
   record with authenticated actor and server timestamp.
9. **Delivery:** changing to `entregada` sets `fechaEntrega`, preserves total, and
   moves the order to the past-order section on reload.
10. **Comment:** blank text returns `400`; valid text returns `201` and appears in
    the next detail response with server-derived author/time.
11. **Upload:** a valid JPEG, MP4, or PDF returns `201`, an HTTPS URL, detected
    category, and timeline timestamp.
12. **Invalid file:** MIME spoofing returns `400`, unsupported types return `415`,
    and oversized files return `413`; no database/media orphan remains.
13. **Service addition:** an active service UUID produces a snapshotted line and
    `201`; a service from another tenant is indistinguishable from not found.
14. **Product addition:** sufficient stock produces a snapshotted line and atomic
    stock decrement; insufficient/concurrent stock returns `409` with no partial
    additions.
15. **Unauthorized access:** missing/expired JWT returns the standard `401`
    envelope for every protected endpoint.
16. **Unknown order:** both an unknown UUID and unknown `numeroOrden` return the
    standard `404` envelope.

## 11. Definition of done

- Backend controller/integration tests cover every acceptance scenario above.
- API responses conform to the common error envelope, including security failures.
- Existing web consumers continue to parse unchanged fields.
- Mobile can run with real services for login, list, detail, state, delivery,
  comments, and media without `EXPO_PUBLIC_USE_MOCKS=true`.
- No route is described as existing unless it is present on backend `develop`;
  status changes in this document occur only when the corresponding backend code
  is merged into that branch.
