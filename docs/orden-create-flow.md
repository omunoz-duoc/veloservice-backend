# Flujo de Creación de Orden: `POST /ordenes`

Este documento describe los DTOs involucrados en la creación de una orden de trabajo y cómo interactúan cuando un cliente realiza una petición `POST /ordenes`.

## Archivos Involucrados

### `NuevaOrdenRequest` (`interfaces.rest.dto`)
- **Rol**: Contrato de entrada de la API. Representa el cuerpo JSON enviado por el cliente.
- **Responsabilidad**: Define la estructura que el cliente debe enviar y contiene anotaciones de validación (`@NotBlank`, `@NotNull`, `@Valid`, etc.).
- **Contenido**: Incluye campos como `clienteId`, `bicicletaId`, `tipoTrabajo`, `prioridad`, `diagnosticoInicial`, `observacionesCliente`, y clases anidadas para `ClienteNuevoRequest`, `BicicletaNuevaRequest`, `ServicioItem` y `ProductoItem`.

### `OrdenCreateCommand` (`application.dto`)
- **Rol**: Objeto de entrada para la capa de aplicación (servicio).
- **Responsabilidad**: Transporta los datos ya validados desde el controlador hacia la lógica de negocio.
- **Contenido**: Similar a `NuevaOrdenRequest` pero sin anotaciones de validación. Reutiliza `ClienteCreateCommand` y `BicicletaCreateCommand` del módulo `clientes`. Además incluye listas de `servicios` (UUIDs) y `productos` (con `productoId` y `cantidad`).

### `OrdenCreadaResult` (`application.dto`)
- **Rol**: Objeto de salida de la capa de aplicación (servicio).
- **Responsabilidad**: Transporta el resultado mínimo necesario después de que la orden fue persistida exitosamente.
- **Contenido**: Contiene únicamente el `id` (UUID) y el `numeroOrden` (String) generado.

### `OrdenCreadaResponse` (`interfaces.rest.dto`)
- **Rol**: Contrato de salida de la API. Representa el cuerpo JSON que se devuelve al cliente.
- **Responsabilidad**: Define explícitamente la forma de la respuesta HTTP `201 Created`.
- **Contenido**: Contiene el `id` (UUID) y el `numeroOrden` (String). Es un espejo de `OrdenCreadaResult` pero vive en la capa REST.

## Diferencias Clave

| Aspecto | `NuevaOrdenRequest` | `OrdenCreateCommand` | `OrdenCreadaResult` | `OrdenCreadaResponse` |
|---------|--------------------|--------------------|--------------------|--------------------|
| **Capa** | Interfaces (REST) | Aplicación | Aplicación | Interfaces (REST) |
| **Dirección** | Entrada | Entrada | Salida | Salida |
| **Validación** | Sí (`@Valid`, `@NotBlank`, etc.) | No (ya validado) | No | No |
| **Propósito** | Definir contrato API | Transportar datos al servicio | Devolver resultado del servicio | Definir respuesta API |

## Flujo Paso a Paso (`POST /ordenes`)

```
Cliente
   |
   | 1. POST /ordenes
   |    Body: { "clienteId": "...", "tipoTrabajo": "...", ... }
   v
Spring Boot
   |
   | 2. Deserializa JSON → NuevaOrdenRequest
   | 3. Ejecuta @Valid (falla → 400 Bad Request)
   v
OrdenController.crear(NuevaOrdenRequest request)
   |
   | 4. toCommand(request)
   |    Mapea NuevaOrdenRequest → OrdenCreateCommand
   |    (incluye mapeo de ClienteNuevoRequest → ClienteCreateCommand,
   |     BicicletaNuevaRequest → BicicletaCreateCommand, etc.)
   v
OrdenService.crear(OrdenCreateCommand command)
   |
   | 5. Resuelve taller, sucursal, cliente y bicicleta
   | 6. Genera numeroOrden mediante secuenciaService
   | 7. Persiste la entidad Orden
   v
OrdenService
   |
   | 8. return new OrdenCreadaResult(ordenId, numeroOrden)
   v
OrdenController.crear(...)
   |
   | 9. new OrdenCreadaResponse(result.id(), result.numeroOrden())
   v
Spring Boot
   |
   | 10. Serializa OrdenCreadaResponse → JSON
   | 11. HTTP 201 CREATED
   v
Cliente
```

### Descripción de los pasos

1. **Llegada de la petición HTTP**: El cliente envía un `POST` a `/ordenes` con un cuerpo JSON.
2. **Deserialización y validación**: Spring Boot convierte el JSON en una instancia de `NuevaOrdenRequest`. La anotación `@Valid` activa las restricciones. Si alguna falla, se devuelve `400 Bad Request` inmediatamente.
3. **Recepción en el controlador**: El método `crear()` de `OrdenController` recibe el objeto `NuevaOrdenRequest` ya validado.
4. **Mapeo al comando de aplicación**: El método privado `toCommand()` transforma `NuevaOrdenRequest` en `OrdenCreateCommand`. Este paso actúa como **capa anti-corruption** entre la API REST y la lógica de aplicación.
5. **Ejecución del servicio**: Se invoca `ordenService.crear(command)`. El servicio se encarga de:
   - Resolver el `tallerId` y `sucursalId` del contexto actual.
   - Buscar o crear el cliente y la bicicleta asociados.
   - Generar el número de orden mediante `secuenciaService`.
   - Persistir la entidad `Orden` en la base de datos.
6. **Retorno del resultado**: El servicio devuelve un `OrdenCreadaResult`, que contiene solo el `id` y el `numeroOrden` de la orden recién creada.
7. **Mapeo a la respuesta REST**: El controlador construye un `OrdenCreadaResponse` a partir de los datos del resultado.
8. **Serialización y respuesta HTTP**: Spring Boot convierte `OrdenCreadaResponse` a JSON y lo envía al cliente con el estado HTTP `201 Created`.
