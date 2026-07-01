# Spec: Auditoría de movimientos de stock

## Objetivo

Registrar un `MovimientoStock` cada vez que el stock de un producto cambia, trazando origen, cantidad, y estado antes/después. Actualmente solo `CompraService` lo hace; hay dos gaps en `OrdenService` y uno en `ProductoService`.

---

## Contexto: patrón existente (CompraService)

```java
MovimientoStock.builder()
    .productoId(producto.getId())
    .compraId(compra.getId())      // origen específico
    .usuarioId(usuarioId)
    .tipo(TipoMovimientoEnum.entrada)
    .cantidad(linea.getCantidad())
    .stockAnterior(stockAnterior)
    .stockPosterior(stockNuevo)
    .motivo("Recepcion de compra #" + compra.getNumeroFactura())
    .build();
```

Todos los nuevos movimientos deben seguir el mismo patrón.

---

## Triggers a implementar

### T1 — Orden creada con productos (`crearEnSucursal`)

**Archivo:** `OrdenService.java`, método `crearEnSucursal`, bloque `if (command.getProductos() != null)` (~línea 1002)

**Problema adicional:** actualmente este bloque guarda `OrdenProducto` pero **no decrementa stock**. Hay que corregir ambas cosas juntas.

**Comportamiento esperado:**
- Por cada `ProductoItem` en `command.getProductos()`:
  1. Leer `producto.getStock()` como `stockAnterior`
  2. Decrementar `producto.setStock(stockAnterior - item.getCantidad())`
  3. Guardar `productoRepository.save(producto)`
  4. Crear `MovimientoStock`:
     - `tipo` → `salida`
     - `cantidad` → `item.getCantidad()`
     - `stockAnterior` / `stockPosterior`
     - `ordenId` → `ordenId` (disponible en ese scope)
     - `usuarioId` → `UsuarioContext.getCurrentUser()`
     - `motivo` → `"Orden creada: " + numeroOrden`

**Guardia:** si `producto.getStock() == null`, omitir decremento y movimiento (igual que en `agregarProductosAOrden`).

**No aplica:** si `proporcionadoPorCliente == true` (en `crearEnSucursal` siempre es `false`, sin cambios necesarios aquí).

---

### T2 — Producto agregado a orden existente (`agregarProductosAOrden`)

**Archivo:** `OrdenService.java`, método `agregarProductosAOrden`, bloque que ya decrementa stock (~línea 742)

```java
// código actual
if (!proporcionadoPorCliente && producto.getStock() != null) {
    producto.setStock(producto.getStock() - item.getCantidad());
    producto.setUpdatedAt(now);
    productoRepository.save(producto);
}
```

**Comportamiento esperado:** después del `productoRepository.save`, crear `MovimientoStock`:
- `tipo` → `salida`
- `cantidad` → `item.getCantidad()`
- `stockAnterior` → capturar **antes** del decremento (`int stockAnterior = producto.getStock()`)
- `stockPosterior` → `producto.getStock()` (después del decremento)
- `ordenId` → `orden.getId()`
- `usuarioId` → `usuarioId` (ya disponible en el método)
- `motivo` → `"Producto agregado a orden: " + orden.getNumeroOrden()`

**No aplica:** si `proporcionadoPorCliente == true` (sin cambio de stock, sin movimiento).

---

### T3 — Stock actualizado vía `PUT /productos/{id}` (`ProductoService.actualizar`)

**Archivo:** `ProductoService.java`, método `actualizar` (~línea 76)

**Comportamiento esperado:**
1. Antes de `producto.setStock(stock)`, capturar `int stockAnterior = producto.getStock() == null ? 0 : producto.getStock()`
2. Después de `productoRepository.save(producto)`, si `stockAnterior != stock`:
   - Crear `MovimientoStock`:
     - `tipo` → `ajuste`
     - `cantidad` → `Math.abs(stock - stockAnterior)` (magnitud del cambio)
     - `stockAnterior` / `stockPosterior` → `stock`
     - `usuarioId` → `UsuarioContext.getCurrentUser()`
     - `motivo` → `"Ajuste manual de stock"`
     - `ordenId`, `compraId`, `trasladoId` → `null`

**Nota:** si el stock no cambia (`stockAnterior == stock`), no crear movimiento.

---

## Diseño: centralización con StockMovimientoService

Para evitar duplicar la lógica de build+save en tres servicios distintos, introducir:

```
inventario/application/usecase/StockMovimientoService.java
```

Con un método único:

```java
public void registrar(
    UUID productoId,
    UUID ordenId,       // nullable
    UUID compraId,      // nullable
    UUID trasladoId,    // nullable
    TipoMovimientoEnum tipo,
    int cantidad,
    int stockAnterior,
    int stockPosterior,
    String motivo
)
```

Este método:
1. Lee `UsuarioContext.getCurrentUser()` para `usuarioId`
2. Construye y persiste el `MovimientoStock`

`OrdenService` y `ProductoService` inyectan `StockMovimientoService` en lugar de `MovimientoStockRepository` directamente.  
`CompraService` puede migrarse al mismo servicio en una tarea separada.

---

## Campos por trigger (resumen)

| Campo           | T1 (orden creada) | T2 (agregar a orden) | T3 (ajuste manual) |
|-----------------|-------------------|----------------------|--------------------|
| `productoId`    | producto.getId()  | producto.getId()     | producto.getId()   |
| `ordenId`       | ordenId           | orden.getId()        | null               |
| `compraId`      | null              | null                 | null               |
| `trasladoId`    | null              | null                 | null               |
| `tipo`          | `salida`          | `salida`             | `ajuste`           |
| `cantidad`      | item.getCantidad()| item.getCantidad()   | abs(nuevo-anterior)|
| `stockAnterior` | antes decremento  | antes decremento     | antes actualización|
| `stockPosterior`| después decremento| después decremento   | nuevo valor        |
| `motivo`        | "Orden creada: X" | "Producto agregado a orden: X" | "Ajuste manual de stock" |

---

## Constraints

- El `MovimientoStock` debe guardarse **en la misma transacción** que el cambio de stock. Si el save del producto falla, no debe quedar movimiento huérfano.
- `stockAnterior` y `stockPosterior` deben capturarse **antes y después** del `setStock`, no calcularse a posteriori.
- Si `producto.getStock() == null`, no se genera movimiento (no hay valor "anterior" fiable). Esto aplica a T1 y T2.
- Para T3: si `stockAnterior == stock`, no generar movimiento (sin cambio real).

---

## Archivos a tocar

| Archivo | Cambio |
|---------|--------|
| `inventario/application/usecase/StockMovimientoService.java` | Crear (nuevo) |
| `ordenes/application/usecase/OrdenService.java` | Inyectar `StockMovimientoService`; T1 + T2 |
| `inventario/application/usecase/ProductoService.java` | Inyectar `StockMovimientoService`; T3 |

No se requieren cambios en schema, repositorios ni DTOs.
