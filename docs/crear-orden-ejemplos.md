# Crear Orden - Ejemplos con `mock_data_v3.sql`

Estos ejemplos usan los IDs de `mock_data_v3.sql` y el endpoint `POST /ordenes`.

## Datos Base

Usuarios demo:

| Rol | Email | Scope JWT esperado |
| --- | --- | --- |
| `admin_taller` | `admin@andespedal.cl` | `taller_id`, sin `sucursal_id` |
| `jefe_taller` | `jefe@andespedal.cl` | `taller_id` + `sucursal_id` |
| `mecanico` | `mecanico@andespedal.cl` | `taller_id` + `sucursal_id` |
| `recepcionista` | `recepcion@andespedal.cl` | `taller_id` + `sucursal_id` |

Taller Andes Pedal:

```text
tallerId: 10000000-0000-4000-8000-000000000001
Providencia sucursalId: 11000000-0000-4000-8000-000000000001
La Florida sucursalId: 11000000-0000-4000-8000-000000000002
Mecanico Diego Pizarro userId: 15000000-0000-4000-8000-000000000003
```

Clientes y bicicletas existentes:

```text
Francisca Contreras clienteId: 19000000-0000-4000-8000-000000000001
Specialized Tarmac bicicletaId: 20000000-0000-4000-8000-000000000001

Rodrigo Ahumada clienteId: 19000000-0000-4000-8000-000000000002
Trek Marlin bicicletaId: 20000000-0000-4000-8000-000000000002
```

Catalogos:

```text
tipoTrabajo validos: mantencion, reparacion, revision, armado, garantia, personalizacion
prioridad validas: baja, media, alta, urgente

Mantencion general servicioId: 21000000-0000-4000-8000-000000000001
Ajuste de transmision servicioId: 21000000-0000-4000-8000-000000000003

Providencia productoId: 24000000-0000-4000-8000-000000000001
La Florida productoId: 24000000-0000-4000-8000-000000000004
```

## Regla por Rol

`admin_taller` debe enviar `sucursalId`, porque su JWT no trae `sucursal_id`.

Los roles sucursal-scoped (`jefe_taller`, `mecanico`, `recepcionista`) no necesitan enviar `sucursalId`; el backend usa la sucursal del JWT. Si envian `sucursalId`, debe ser igual al `sucursal_id` del JWT.

## 1. Admin Taller - Cliente Existe y Bicicleta Existe

Usar token de `admin@andespedal.cl`. Como el JWT solo trae `taller_id`, se debe enviar `sucursalId`.

```bash
curl -X POST http://localhost:8080/ordenes \
  -H "Authorization: Bearer <ADMIN_TALLER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "sucursalId": "11000000-0000-4000-8000-000000000001",
    "clienteId": "19000000-0000-4000-8000-000000000001",
    "bicicletaId": "20000000-0000-4000-8000-000000000001",
    "tipoTrabajo": "mantencion",
    "prioridad": "media",
    "mecanicoId": "15000000-0000-4000-8000-000000000003",
    "fechaPrometida": "2026-06-05",
    "diagnosticoInicial": "Revision completa antes de salida larga.",
    "observacionesCliente": "Cliente pide revisar presion y transmision.",
    "servicios": [
      { "servicioId": "21000000-0000-4000-8000-000000000001" }
    ],
    "productos": [
      { "productoId": "24000000-0000-4000-8000-000000000001", "cantidad": 1 }
    ]
  }'
```

## 2. Admin Taller - Cliente Existe y Bicicleta Nueva

La bicicleta se crea asociada al cliente existente del taller.

```json
{
  "sucursalId": "11000000-0000-4000-8000-000000000001",
  "clienteId": "19000000-0000-4000-8000-000000000002",
  "bicicletaNueva": {
    "marca": "Cannondale",
    "modelo": "Quick 4",
    "tipo": "Urbana",
    "aro": "700c",
    "color": "Verde",
    "numeroSerie": "CND-QUI-2024-7788",
    "anio": 2024,
    "notas": "Bicicleta nueva del cliente para mantencion inicial."
  },
  "tipoTrabajo": "revision",
  "prioridad": "baja",
  "fechaPrometida": "2026-06-06",
  "diagnosticoInicial": "Revision de armado y ajustes basicos.",
  "servicios": [
    { "servicioId": "21000000-0000-4000-8000-000000000003" }
  ]
}
```

## 3. Admin Taller - Cliente Nuevo y Bicicleta Nueva

Esta es la combinacion normal para un cliente que llega por primera vez.

```json
{
  "sucursalId": "11000000-0000-4000-8000-000000000002",
  "clienteNuevo": {
    "nombre": "Paula",
    "apellido": "Herrera",
    "rut": "16.778.221-4",
    "telefono": "+56981112233",
    "email": "paula.herrera@mail.cl",
    "direccion": "Av. La Florida 7200, La Florida"
  },
  "bicicletaNueva": {
    "marca": "Scott",
    "modelo": "Scale 970",
    "tipo": "MTB",
    "aro": "29",
    "color": "Negro",
    "numeroSerie": "SCT-SCL-2023-9012",
    "anio": 2023,
    "notas": "Uso recreativo fin de semana."
  },
  "tipoTrabajo": "reparacion",
  "prioridad": "alta",
  "mecanicoId": "15000000-0000-4000-8000-000000000003",
  "fechaPrometida": "2026-06-04",
  "diagnosticoInicial": "Cambio duro y ruido en caja motor.",
  "observacionesCliente": "Avisar antes de cambiar repuestos.",
  "productos": [
    { "productoId": "24000000-0000-4000-8000-000000000004", "cantidad": 1 }
  ]
}
```

## 4. Usuario de Sucursal - Cliente Existe y Bicicleta Existe

Usar token de `recepcion@andespedal.cl`, `mecanico@andespedal.cl` o `jefe@andespedal.cl`. En `mock_data_v3.sql`, estos usuarios tienen sucursal principal Providencia.

No enviar `sucursalId`; el backend usa `11000000-0000-4000-8000-000000000001` desde el JWT.

```bash
curl -X POST http://localhost:8080/ordenes \
  -H "Authorization: Bearer <SUCURSAL_SCOPED_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "19000000-0000-4000-8000-000000000003",
    "bicicletaId": "20000000-0000-4000-8000-000000000003",
    "tipoTrabajo": "revision",
    "prioridad": "media",
    "fechaPrometida": "2026-06-07",
    "diagnosticoInicial": "Ruido al pedalear en subida.",
    "observacionesCliente": "Enviar presupuesto por WhatsApp.",
    "servicios": [
      { "servicioId": "21000000-0000-4000-8000-000000000003" }
    ]
  }'
```

## 5. Usuario de Sucursal - Cliente Existe y Bicicleta Nueva

Valido porque clientes y bicicletas son taller-scoped. La orden queda en la sucursal del JWT.

```json
{
  "clienteId": "19000000-0000-4000-8000-000000000004",
  "bicicletaNueva": {
    "marca": "Bianchi",
    "modelo": "Impulso",
    "tipo": "Ruta",
    "aro": "700c",
    "color": "Celeste",
    "numeroSerie": "BIA-IMP-2022-4455",
    "anio": 2022
  },
  "tipoTrabajo": "mantencion",
  "prioridad": "media",
  "mecanicoId": "15000000-0000-4000-8000-000000000003",
  "fechaPrometida": "2026-06-08",
  "diagnosticoInicial": "Mantencion por compra de bicicleta usada."
}
```

## 6. Usuario de Sucursal - Cliente Nuevo y Bicicleta Nueva

El cliente se crea en el taller y queda visible para todas las sucursales del taller. La orden queda en la sucursal del JWT.

```json
{
  "clienteNuevo": {
    "nombre": "Ignacio",
    "apellido": "Morales",
    "rut": "18.445.990-2",
    "telefono": "+56987776655",
    "email": "ignacio.morales@mail.cl",
    "direccion": "Los Conquistadores 1800, Providencia"
  },
  "bicicletaNueva": {
    "marca": "Giant",
    "modelo": "Talon 2",
    "tipo": "MTB",
    "aro": "29",
    "color": "Rojo",
    "numeroSerie": "GNT-TAL-2024-1001",
    "anio": 2024
  },
  "tipoTrabajo": "armado",
  "prioridad": "baja",
  "fechaPrometida": "2026-06-10",
  "diagnosticoInicial": "Armado y ajuste de bicicleta nueva en caja."
}
```

## Casos Invalidos Esperados

Admin taller sin `sucursalId`:

```json
{
  "clienteId": "19000000-0000-4000-8000-000000000001",
  "bicicletaId": "20000000-0000-4000-8000-000000000001",
  "tipoTrabajo": "mantencion",
  "prioridad": "media"
}
```

Resultado esperado: error porque `admin_taller` no tiene `sucursal_id` en el JWT.

Admin taller usando sucursal de otro taller:

```json
{
  "sucursalId": "11000000-0000-4000-8000-000000000003",
  "clienteId": "19000000-0000-4000-8000-000000000001",
  "bicicletaId": "20000000-0000-4000-8000-000000000001",
  "tipoTrabajo": "mantencion",
  "prioridad": "media"
}
```

Resultado esperado: error porque Casa Matriz Temuco pertenece a Ruta Sur Bicicletas, no a Andes Pedal.

Usuario sucursal-scoped intentando crear en otra sucursal:

```json
{
  "sucursalId": "11000000-0000-4000-8000-000000000002",
  "clienteId": "19000000-0000-4000-8000-000000000001",
  "bicicletaId": "20000000-0000-4000-8000-000000000001",
  "tipoTrabajo": "mantencion",
  "prioridad": "media"
}
```

Resultado esperado: error si el JWT trae `sucursal_id = 11000000-0000-4000-8000-000000000001`.

Cliente nuevo con bicicleta existente:

```json
{
  "clienteNuevo": {
    "nombre": "Cliente",
    "apellido": "Nuevo",
    "rut": "19.111.222-3"
  },
  "bicicletaId": "20000000-0000-4000-8000-000000000001",
  "tipoTrabajo": "revision",
  "prioridad": "media"
}
```

Resultado esperado: error porque la bicicleta existente pertenece a otro cliente. Para cliente nuevo se debe enviar `bicicletaNueva`.

Cliente existente con bicicleta de otro cliente:

```json
{
  "clienteId": "19000000-0000-4000-8000-000000000001",
  "bicicletaId": "20000000-0000-4000-8000-000000000002",
  "tipoTrabajo": "revision",
  "prioridad": "media"
}
```

Resultado esperado: error porque la bicicleta de Rodrigo no pertenece a Francisca.

Producto de otra sucursal:

```json
{
  "sucursalId": "11000000-0000-4000-8000-000000000001",
  "clienteId": "19000000-0000-4000-8000-000000000001",
  "bicicletaId": "20000000-0000-4000-8000-000000000001",
  "tipoTrabajo": "reparacion",
  "prioridad": "media",
  "productos": [
    { "productoId": "24000000-0000-4000-8000-000000000004", "cantidad": 1 }
  ]
}
```

Resultado esperado: error porque el producto `24000000-0000-4000-8000-000000000004` pertenece a La Florida, pero la orden se esta creando en Providencia.

## Matriz Rapida

| Scope usuario | `sucursalId` en request | Cliente | Bicicleta | Valido |
| --- | --- | --- | --- | --- |
| `admin_taller` | Sucursal del taller | existente | existente del cliente | Si |
| `admin_taller` | Sucursal del taller | existente | nueva | Si |
| `admin_taller` | Sucursal del taller | nuevo | nueva | Si |
| `admin_taller` | omitido | cualquiera | cualquiera | No |
| `admin_taller` | sucursal de otro taller | cualquiera | cualquiera | No |
| sucursal-scoped | omitido | existente | existente del cliente | Si |
| sucursal-scoped | omitido | existente | nueva | Si |
| sucursal-scoped | omitido | nuevo | nueva | Si |
| sucursal-scoped | igual al JWT | cualquiera valido | cualquiera valida | Si |
| sucursal-scoped | distinta al JWT | cualquiera | cualquiera | No |
| cualquier rol | segun scope | nuevo | existente | No |
| cualquier rol | segun scope | existente | existente de otro cliente | No |
