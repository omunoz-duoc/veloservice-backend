Los que esten no los consideres pero igual los mandé 
## 1. AUTH

### POST `auth/login`

Request:

```json
{
  "email": "admin@veloservice.cl",
  "password": "123456"
}
```

Response:

```json
{
  "id": "user-1",
  "nombre": "Valentina",
  "apellido": "Soto",
  "token": "jwt_token",
  "rol": "ADMIN",
  "ambito": "taller",
  "tallerId": "taller-1",
  "sucursalId": "10000000-0000-4000-8000-000000000001"
}
```

### GET `sucursales`

Response:

```json
[
  {
    "id": "10000000-0000-4000-8000-000000000001",
    "nombre": "Sucursal Central"
  }
]
```

### POST `auth/register`

Request:

```json
{
  "nombre": "Valentina",
  "apellido": "Soto",
  "rut": "12.345.678-9",
  "telefono": "+56912345678",
  "email": "valentina@veloservice.cl",
  "rol": "ADMIN",
  "sucursalId": "10000000-0000-4000-8000-000000000001",
  "password": "123456"
}
```

### POST `auth/verify-code`

Request:

```json
{
  "code": "123456"
}
```

Response:

```json
true
```

### POST `auth/reset-password`

Request recuperación:

```json
{
  "email": "valentina@veloservice.cl"
}
```

Request cambio password:

```json
{
  "password": "NuevaPassword123"
}
```

### POST `auth/logout`

Response puede ser vacío.

---

## 2. DASHBOARD

### GET `dashboard/kpis/active-ots`

Response:

```json
{
  "count": 12
}
```

### GET `dashboard/kpis/done-ots`

Response:

```json
{
  "count": 8
}
```

### GET `dashboard/kpis/daily-financial`

Response:

```json
{
  "revenue": 6900000,
  "expenses": 4000000,
  "profit": 2900000
}
```

### GET `dashboard/kpis/low-stock`

Response:

```json
{
  "items": [
    {
      "name": "Cadena Shimano",
      "stock": 2
    }
  ]
}
```

### GET `dashboard/pipeline-columns`

Response:

```json
[
  {
    "name": "Recibidas",
    "otCount": 3
  },
  {
    "name": "En proceso",
    "otCount": 5
  }
]
```

### GET `dashboard/ordenes-urgentes`

Response:

```json
[
  {
    "id": "ot-1",
    "cliente": "Juan Pérez",
    "fechaEntrega": "2026-06-12"
  }
]
```

### GET `dashboard/mecanicos-activos`

Response:

```json
[
  {
    "id": "mec-1",
    "nombre": "Carlos Soto",
    "otAsignadas": 3
  }
]
```

### GET `dashboard/actividades-recientes`

Response:

```json
[
  {
    "id": "act-1",
    "descripcion": "Orden OT-001 actualizada",
    "fecha": "2026-06-09T15:30:00"
  }
]
```

### GET `dashboard/rentabilidad-mensual`

Response:

```json
[
  {
    "mes": "Junio",
    "rentabilidad": 42
  }
]
```

---

## 3. ÓRDENES

### GET `ordenes`

Response:

```json
{
  "total": 1,
  "ordenes": [
    {
      "id": "ot-1",
      "numeroOrden": "OT-0001",
      "tipo": {
        "id": "tipo-1",
        "codigo": "mantencion",
        "nombre": "Mantención"
      },
      "estado": {
        "id": "estado-1",
        "codigo": "recibida",
        "nombre": "Recibida"
      },
      "fechaIngreso": "2026-06-09",
      "fechaPrometida": "2026-06-12",
      "observacionesCliente": "Hace ruido al frenar",
      "bicicleta": {
        "id": "bici-1",
        "marca": "Trek",
        "modelo": "Marlin 5",
        "tipo": "MTB",
        "aro": "29",
        "color": "Negro",
        "numeroSerie": "ABC123"
      },
      "cliente": {
        "id": "cli-1",
        "nombre": "Juan",
        "apellido": "Pérez",
        "telefono": "+56912345678",
        "email": "juan@mail.com",
        "rut": "12.345.678-9"
      },
      "mecanico": {
        "id": "mec-1",
        "nombre": "Carlos",
        "apellido": "Soto"
      },
      "diagnosticoInicial": "Revisión general",
      "prioridad": "alta"
    }
  ]
}
```

### GET `ordenes/metricas`

Response:

```json
{
  "recibidas": 4,
  "enProceso": 5,
  "listas": 2,
  "entregadas": 10
}
```

### GET `ordenes/urgentes`

Response:

```json
{
  "total": 1,
  "ordenes": []
}
```

### GET `ordenes/tipos`

Response:

```json
[
  {
    "id": "tipo-1",
    "codigo": "mantencion",
    "nombre": "Mantención"
  }
]
```

### GET `ordenes/:id`

Response: detalle completo de la OT.

```json
{
  "id": "ot-1",
  "numeroOrden": "OT-0001",
  "tallerId": "taller-1",
  "sucursalId": "sucursal-1",
  "estado": {
    "codigo": "recibida",
    "nombre": "Recibida"
  },
  "tipo": {
    "codigo": "mantencion",
    "nombre": "Mantención"
  },
  "fechaIngreso": "2026-06-09",
  "fechaPrometida": "2026-06-12",
  "diagnosticoInicial": "Revisión general",
  "observacionesCliente": "Hace ruido",
  "prioridad": "alta",
  "bicicleta": {},
  "cliente": {},
  "mecanico": {},
  "comentarios": [],
  "multimedia": [],
  "productos": [],
  "servicios": []
}
```

### POST `ordenes`

Request nueva OT:

```json
{
  "clienteId": "cli-1",
  "bicicletaId": "bici-1",
  "tipoTrabajo": "mantencion",
  "prioridad": "alta",
  "mecanicoId": "mec-1",
  "fechaPrometida": "2026-06-12",
  "diagnosticoInicial": "Revisión general",
  "observacionesCliente": "Hace ruido al frenar",
  "servicios": [
    {
      "servicioId": "serv-1"
    }
  ],
  "productos": [
    {
      "productoId": "prod-1",
      "cantidad": 2
    }
  ]
}
```

Response:

```json
{
  "id": "ot-1",
  "numeroOrden": "OT-0001"
}
```

### PATCH `ordenes/:id/estado`

Request:

```json
{
  "codigo": "en_proceso",
  "observacion": "Orden tomada por mecánico"
}
```

Response: detalle actualizado de la orden.

### PATCH `ordenes/:id`

Request:

```json
{
  "estadoCodigo": "en_proceso",
  "estadoObservacion": "Avance actualizado",
  "tipoCodigo": "mantencion",
  "prioridad": "media",
  "mecanicoId": "mec-1",
  "fechaEstimada": "2026-06-12",
  "notasInternas": "Cliente pidió aviso por WhatsApp"
}
```

### PATCH `ordenes/bulk`

Request:

```json
{
  "ids": ["ot-1", "ot-2"],
  "estado": "en_proceso",
  "mecanicoId": "mec-1"
}
```

### DELETE `ordenes/:id`

Response vacío.

---

## 4. CLIENTES

### GET `clientes`

Response:

```json
{
  "total": 1,
  "clientes": [
    {
      "id": "cli-1",
      "nombre": "Juan",
      "apellido": "Pérez",
      "tipo": "regular",
      "rut": "12.345.678-9",
      "email": "juan@mail.com",
      "telefono": "+56912345678",
      "bicicletasCount": 2,
      "ordenesCount": 5,
      "totalGastado": 120000,
      "ciudad": "Santiago",
      "fechaReg": "2026-06-01",
      "ultimaVisita": "2026-06-09",
      "canal": "WhatsApp",
      "notas": "Cliente frecuente",
      "consentEmail": true,
      "consentWhatsApp": true,
      "consentMarketing": false
    }
  ]
}
```

### POST `clientes`

Request:

```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "rut": "12.345.678-9",
  "email": "juan@mail.com",
  "telefono": "+56912345678"
}
```

### GET `clientes/lista-clientes`

Response:

```json
[
  {
    "id": "cli-1",
    "nombre": "Juan Pérez",
    "rut": "12.345.678-9"
  }
]
```

### GET `bicicletas?clienteId=cli-1`

Response:

```json
[
  {
    "id": "bici-1",
    "marca": "Trek",
    "modelo": "Marlin 5",
    "tipo": "MTB",
    "color": "Negro",
    "numSerie": "ABC123",
    "anio": 2022
  }
]
```

---

## 5. SERVICIOS

### GET `servicios`

Response:

```json
{
  "total": 1,
  "servicios": [
    {
      "id": "serv-1",
      "nombre": "Ajuste de frenos",
      "descripcion": "Ajuste y revisión de frenos",
      "precioBase": 15000,
      "activo": true
    }
  ]
}
```

### POST `servicios`

Request:

```json
{
  "cat": "mantencion",
  "nombre": "Ajuste de frenos",
  "precio": 15000,
  "precio2": 20000,
  "dur": 30,
  "desc": "Ajuste y revisión de frenos",
  "incluye": ["Revisión", "Ajuste"],
  "skills": ["frenos"],
  "activo": true,
  "popular": false
}
```

Valores válidos para `cat`:

```txt
rapidos, mantencion, ruedas, ebike, kids, logistica
```

---

## 6. INVENTARIO / PRODUCTOS

### GET `productos`

Response:

```json
{
  "total": 1,
  "productos": [
    {
      "id": "prod-1",
      "nombre": "Cadena Shimano",
      "sku": "CAD-SHI-001",
      "categoria": "Transmisión",
      "costoUnitario": 5000,
      "precioAsignado": 9900,
      "stock": 10
    }
  ]
}
```

### GET `productos/metricas`

Response:

```json
{
  "valorInventario": 50000,
  "enStock": 10,
  "stockBajo": 2,
  "agotados": 0
}
```

### POST `productos`

Request:

```json
{
  "nombre": "Cadena Shimano",
  "sku": "CAD-SHI-001",
  "categoria": "Transmisión",
  "costoUnitario": 5000,
  "precioAsignado": 9900,
  "stock": 10
}
```

### GET `productos/lista-productos`

Response:

```json
{
  "productos": [
    {
      "id": "prod-1",
      "nombre": "Cadena Shimano",
      "precioVenta": 9900,
      "stock": 10
    }
  ]
}
```

---

## 7. MECÁNICOS

### GET `mecanicos/activos`

Response:

```json
{
  "total": 1,
  "mecanicos": [
    {
      "id": "mec-1",
      "nombre": "Carlos",
      "apellido": "Soto",
      "iniciales": "CS",
      "color": "#2563eb",
      "especialidad": "Transmisión",
      "bahia": "Bahía 1",
      "horas": "09:00-18:00",
      "estado": "activo",
      "ordenesCursando": [
        {
          "id": "ot-1"
        }
      ],
      "capacidad": 5
    }
  ]
}
```

Valores de `estado`:

```txt
activo, saturado, pausa
```

### PUT `mecanicos/:id`

Request cambio estado:

```json
{
  "estado": "pausa"
}
```

Request cambio rol:

```json
{
  "rol": "jefe_taller"
}
```

### GET `administracion/lista-mecanicos`

Response:

```json
[
  {
    "id": "mec-1",
    "nombre": "Carlos Soto"
  }
]
```

---

## 8. FINANZAS

### GET `finanzas/metricas`

Response:

```json
{
  "cobrosDelDia": 150000
}
```

---