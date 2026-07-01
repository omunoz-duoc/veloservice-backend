# Structural Refactor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Corregir 5 violaciones arquitectónicas que bloquean la escalabilidad del proyecto antes de agregar más features.

**Architecture:** Modular monolith con capas DDD por módulo (domain → application → infraestructure → interfaces). Cada tarea es independiente y produce código compilable+testeable sin romper funcionalidad existente.

**Tech Stack:** Spring Boot 3.3, Java 21, JPA/Hibernate 6.5, JUnit 5, Mockito, Maven

---

## Mapa de problemas

| # | Problema | Impacto escalabilidad | Archivos afectados |
|---|----------|-----------------------|--------------------|
| P1 | `OrdenController` inyecta `OrdenRepository` directamente | Alto — viola separación de capas | `OrdenController.java`, `OrdenService.java` |
| P2 | `OrdenService` importa repositorios de 4 módulos externos vía `infraestructure` | Alto — acoplamiento estructural | `OrdenService.java`, módulos externos |
| P3 | Enums de negocio en `config/enums/` global | Medio — cualquier módulo nuevo contamina config | 14 enums + todos sus importadores |
| P4 | DTOs request/response mezclados con controllers en `interfaces/rest/` | Medio — difícil navegar, mapper imports sucios | Todos los módulos con controllers |
| P5 | `SucursalContext`, `TallerContext`, `UsuarioContext` en `config/security/` | Bajo — son concerns de tenant, no de security | 3 clases + todos los importadores |

**Orden de ejecución:** P1 → P5 → P2 → P3 → P4 (de menor a mayor riesgo y tamaño)

---

## Task 1: Eliminar Repository del Controller (P1)

El problema concreto: `OrdenController.estados()` inyecta `OrdenRepository` y llama
`findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc()` directamente, ejecutando lógica de negocio
(filtrado de estados, agrupación) en la capa de interfaz.

**Files:**
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Modify: `src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java`
- Modify: `src/test/java/com/veloservice/ordenes/interfaces/rest/OrdenEstadosControllerTest.java`
- Create: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceEstadosTest.java`

- [ ] **Step 1: Escribir test que falla para el nuevo método en OrdenService**

Crear archivo `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceEstadosTest.java`:

```java
package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenServiceEstadosTest {

    @Mock OrdenRepository ordenRepository;
    // otros mocks necesarios para que OrdenService compile
    @Mock com.veloservice.ordenes.infraestructure.persistence.repository.OrdenEstadoRepository ordenEstadoRepository;
    @Mock com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository multimediaRepository;
    @Mock com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository servicioRepository;
    @Mock com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository ordenServicioRepository;
    @Mock com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository productoRepository;
    @Mock com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository ordenProductoRepository;
    @Mock com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository movimientoStockRepository;
    @Mock com.veloservice.ordenes.application.usecase.SecuenciaService secuenciaService;
    @Mock com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository bicicletaRepository;
    @Mock com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository usuarioRepository;

    @InjectMocks OrdenService ordenService;

    private final UUID sucursalId = UUID.randomUUID();
    private final UUID mecanicoId = UUID.randomUUID();

    @BeforeEach
    void setContext() {
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(mecanicoId);
    }

    @AfterEach
    void clearContext() {
        SucursalContext.clear();
        UsuarioContext.clear();
    }

    @Test
    void contarPorEstado_agrupa_canceladas_excluidas() {
        Orden activa = new Orden();
        activa.setEstado(EstadoOrdenEnum.en_reparacion);
        Orden cancelada = new Orden();
        cancelada.setEstado(EstadoOrdenEnum.cancelada);

        when(ordenRepository.findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(sucursalId, mecanicoId))
                .thenReturn(List.of(activa, cancelada));

        Map<String, Long> resultado = ordenService.contarPorEstado();

        assertThat(resultado).containsKey("en_proceso");
        assertThat(resultado).doesNotContainKey("cancelada");
        assertThat(resultado.get("en_proceso")).isEqualTo(1L);
    }

    @Test
    void contarPorEstado_retorna_vacio_sin_contexto() {
        SucursalContext.clear();
        UsuarioContext.clear();

        Map<String, Long> resultado = ordenService.contarPorEstado();

        assertThat(resultado).isEmpty();
    }
}
```

- [ ] **Step 2: Correr el test y verificar que falla**

```bash
./mvnw test -pl . -Dtest=OrdenServiceEstadosTest -q
```
Esperado: `FAIL — method contarPorEstado() not found in OrdenService`

- [ ] **Step 3: Agregar `contarPorEstado()` en OrdenService**

En `OrdenService.java`, agregar después del bloque static `TRANSICIONES_VALIDAS` y de los campos:

```java
private static final java.util.Set<EstadoOrdenEnum> EN_PROCESO_ESTADOS = EnumSet.of(
    EstadoOrdenEnum.en_diagnostico,
    EstadoOrdenEnum.esperando_repuestos,
    EstadoOrdenEnum.en_reparacion,
    EstadoOrdenEnum.control_calidad
);

public Map<String, Long> contarPorEstado() {
    UUID sucursalId = SucursalContext.getCurrentSucursal();
    UUID mecanicoId = UsuarioContext.getCurrentUser();
    if (sucursalId == null || mecanicoId == null) {
        return Map.of();
    }
    return ordenRepository
        .findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(sucursalId, mecanicoId)
        .stream()
        .filter(o -> o.getEstado() != EstadoOrdenEnum.cancelada)
        .collect(Collectors.groupingBy(
            o -> toGrupoEstado(o.getEstado()),
            Collectors.counting()
        ));
}

private static String toGrupoEstado(EstadoOrdenEnum estado) {
    if (EN_PROCESO_ESTADOS.contains(estado)) return "en_proceso";
    return estado.name();
}
```

Nota: eliminar la constante `EN_PROCESO_ESTADOS` y el método `toGrupoEstado` que actualmente
están en `OrdenController` — ya quedan en `OrdenService`.

- [ ] **Step 4: Correr tests y verificar que pasan**

```bash
./mvnw test -pl . -Dtest=OrdenServiceEstadosTest -q
```
Esperado: `BUILD SUCCESS`

- [ ] **Step 5: Actualizar OrdenController para usar el nuevo método**

En `OrdenController.java`:

1. Eliminar la línea:
```java
private final OrdenRepository ordenRepository;
```

2. Eliminar el import:
```java
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
```

3. Eliminar la constante `EN_PROCESO_ESTADOS` y el método privado `toGrupoEstado`.

4. Reemplazar el método `estados()`:
```java
@GetMapping("/estados")
public ResponseEntity<Map<String, Long>> estados() {
    return ResponseEntity.ok(ordenService.contarPorEstado());
}
```

5. Eliminar los imports de `SucursalContext` y `UsuarioContext` **solo si** ya no se usan
   en ningún otro método del controller. Verificar antes de eliminar.

- [ ] **Step 6: Correr toda la suite para verificar sin regresiones**

```bash
./mvnw test -q
```
Esperado: `BUILD SUCCESS` con todos los tests existentes pasando.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/main/java/com/veloservice/ordenes/interfaces/rest/OrdenController.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServiceEstadosTest.java
git commit -m "refactor(ordenes): move estados grouping logic from controller to service"
```

---

## Task 2: Mover ThreadLocal contexts a config/tenant/ (P5)

`SucursalContext`, `TallerContext`, `UsuarioContext` están en `config/security/` pero son
abstracciones de tenant/request scope, no de security. `TenantOperationAspect` ya está en
`config/tenant/` — los contextos deben vivir con él.

**Files:**
- Move: `config/security/SucursalContext.java` → `config/tenant/SucursalContext.java`
- Move: `config/security/TallerContext.java` → `config/tenant/TallerContext.java`
- Move: `config/security/UsuarioContext.java` → `config/tenant/UsuarioContext.java`
- Modify: todos los archivos que importen `com.veloservice.config.security.{Sucursal,Taller,Usuario}Context`

- [ ] **Step 1: Crear los archivos en el nuevo paquete**

Copiar cada clase a `src/main/java/com/veloservice/config/tenant/` cambiando **solo** la
declaración de paquete en la primera línea:

`SucursalContext.java` → cambiar `package com.veloservice.config.security;` por
`package com.veloservice.config.tenant;`

Repetir para `TallerContext.java` y `UsuarioContext.java`.

- [ ] **Step 2: Actualizar todos los imports en producción**

```bash
find src/main/java -name "*.java" -exec grep -l "com.veloservice.config.security.SucursalContext\|com.veloservice.config.security.TallerContext\|com.veloservice.config.security.UsuarioContext" {} \;
```

Para cada archivo encontrado, reemplazar:
- `import com.veloservice.config.security.SucursalContext;` → `import com.veloservice.config.tenant.SucursalContext;`
- `import com.veloservice.config.security.TallerContext;` → `import com.veloservice.config.tenant.TallerContext;`
- `import com.veloservice.config.security.UsuarioContext;` → `import com.veloservice.config.tenant.UsuarioContext;`

`JwtAuthenticationFilter` usa los tres — ese import también cambia.

- [ ] **Step 3: Actualizar todos los imports en tests**

```bash
find src/test/java -name "*.java" -exec grep -l "com.veloservice.config.security.SucursalContext\|com.veloservice.config.security.TallerContext\|com.veloservice.config.security.UsuarioContext" {} \;
```

Aplicar los mismos reemplazos de import.

- [ ] **Step 4: Eliminar los archivos originales en config/security/**

```bash
rm src/main/java/com/veloservice/config/security/SucursalContext.java
rm src/main/java/com/veloservice/config/security/TallerContext.java
rm src/main/java/com/veloservice/config/security/UsuarioContext.java
```

- [ ] **Step 5: Compilar y correr tests**

```bash
./mvnw test -q
```
Esperado: `BUILD SUCCESS`. Si hay errores de compilación, hay un archivo con el import viejo
que no fue actualizado — buscar con grep y corregir.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor(config): move ThreadLocal contexts from security to tenant package"
```

---

## Task 3: Introducir puertos de aplicación para romper acoplamiento cross-módulo (P2)

`OrdenService` importa repositorios de infraestructura de 4 módulos ajenos:
- `UsuarioRepository` (administracion)
- `BicicletaRepository` (clientes)
- `ProductoRepository` (inventario)
- `MovimientoStockRepository` (inventario)
- `ServicioRepository` (servicios)

La solución es definir interfaces (puertos) en el módulo `ordenes/application/port/` y hacer
que los repositorios externos sean adaptadores de esos puertos. Esto permite que `OrdenService`
compile sin importar ningún módulo externo.

Esta tarea aplica el patrón a los dos casos más frecuentes: `UsuarioRepository` y `BicicletaRepository`.
Los otros tres siguen el mismo patrón.

**Files:**
- Create: `src/main/java/com/veloservice/ordenes/application/port/UsuarioPort.java`
- Create: `src/main/java/com/veloservice/ordenes/application/port/BicicletaPort.java`
- Create: `src/main/java/com/veloservice/ordenes/infraestructure/adapter/UsuarioAdapter.java`
- Create: `src/main/java/com/veloservice/ordenes/infraestructure/adapter/BicicletaAdapter.java`
- Modify: `src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java`
- Create: `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServicePortTest.java`

- [ ] **Step 1: Definir el DTO mínimo y el puerto para Usuario**

Crear `src/main/java/com/veloservice/ordenes/application/port/UsuarioPort.java`:

```java
package com.veloservice.ordenes.application.port;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioPort {
    Optional<UsuarioRef> findById(UUID id);

    record UsuarioRef(UUID id, String nombre, String apellido, String email) {}
}
```

- [ ] **Step 2: Definir el puerto para Bicicleta**

Crear `src/main/java/com/veloservice/ordenes/application/port/BicicletaPort.java`:

Inspeccionar qué campos de `Bicicleta` usa `OrdenService`:
```bash
grep -n "bicicleta\." src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java | head -20
```

Crear el puerto con los campos que aparezcan. Ejemplo típico:

```java
package com.veloservice.ordenes.application.port;

import java.util.Optional;
import java.util.UUID;

public interface BicicletaPort {
    Optional<BicicletaRef> findById(UUID id);

    record BicicletaRef(UUID id, UUID clienteId, String marca, String modelo,
                        String tipo, String color, String talla) {}
}
```

Ajustar los campos del record según lo que devuelva el grep del step anterior.

- [ ] **Step 3: Escribir test que falla usando los puertos (en vez de repositorios)**

Crear `src/test/java/com/veloservice/ordenes/application/usecase/OrdenServicePortTest.java`:

```java
package com.veloservice.ordenes.application.usecase;

import com.veloservice.ordenes.application.port.BicicletaPort;
import com.veloservice.ordenes.application.port.UsuarioPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrdenServicePortTest {

    @Mock UsuarioPort usuarioPort;
    @Mock BicicletaPort bicicletaPort;

    @Test
    void usuarioPort_retorna_empty_cuando_no_existe() {
        UUID id = UUID.randomUUID();
        when(usuarioPort.findById(id)).thenReturn(Optional.empty());

        Optional<UsuarioPort.UsuarioRef> result = usuarioPort.findById(id);

        assertThat(result).isEmpty();
    }

    @Test
    void bicicletaPort_retorna_ref_cuando_existe() {
        UUID id = UUID.randomUUID();
        var ref = new BicicletaPort.BicicletaRef(id, UUID.randomUUID(), "Trek", "FX3", "urbana", "negro", "M");
        when(bicicletaPort.findById(id)).thenReturn(Optional.of(ref));

        Optional<BicicletaPort.BicicletaRef> result = bicicletaPort.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().marca()).isEqualTo("Trek");
    }
}
```

- [ ] **Step 4: Correr test para verificar que compila y pasa**

```bash
./mvnw test -pl . -Dtest=OrdenServicePortTest -q
```
Esperado: `BUILD SUCCESS` (las interfaces ya están definidas, el test mockea las interfaces).

- [ ] **Step 5: Crear adaptadores**

Crear `src/main/java/com/veloservice/ordenes/infraestructure/adapter/UsuarioAdapter.java`:

```java
package com.veloservice.ordenes.infraestructure.adapter;

import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.ordenes.application.port.UsuarioPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UsuarioAdapter implements UsuarioPort {

    private final UsuarioRepository usuarioRepository;

    @Override
    public Optional<UsuarioRef> findById(UUID id) {
        return usuarioRepository.findById(id)
                .map(u -> new UsuarioRef(u.getId(), u.getNombre(), u.getApellido(), u.getEmail()));
    }
}
```

Crear `src/main/java/com/veloservice/ordenes/infraestructure/adapter/BicicletaAdapter.java`:

```java
package com.veloservice.ordenes.infraestructure.adapter;

import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.ordenes.application.port.BicicletaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BicicletaAdapter implements BicicletaPort {

    private final BicicletaRepository bicicletaRepository;

    @Override
    public Optional<BicicletaRef> findById(UUID id) {
        return bicicletaRepository.findById(id)
                .map(b -> new BicicletaRef(
                    b.getId(), b.getClienteId(),
                    b.getMarca(), b.getModelo(),
                    b.getTipo(), b.getColor(), b.getTalla()
                ));
    }
}
```

Ajustar los getters según los campos reales de la entidad `Bicicleta`.

- [ ] **Step 6: Migrar OrdenService para usar puertos**

En `OrdenService.java`:

1. Reemplazar los campos:
```java
// ANTES
private final BicicletaRepository bicicletaRepository;
private final UsuarioRepository usuarioRepository;

// DESPUÉS
private final BicicletaPort bicicletaPort;
private final UsuarioPort usuarioPort;
```

2. Eliminar imports:
```java
import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
```

3. Agregar imports:
```java
import com.veloservice.ordenes.application.port.BicicletaPort;
import com.veloservice.ordenes.application.port.UsuarioPort;
```

4. Actualizar todos los usos en los métodos: reemplazar llamadas a `bicicletaRepository.findById(id)`
   por `bicicletaPort.findById(id)`, y adaptar el uso del objeto retornado (ya no es entidad
   `Bicicleta` sino `BicicletaPort.BicicletaRef`).

   Buscar todos los usos con:
   ```bash
   grep -n "bicicletaRepository\|usuarioRepository" src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java
   ```

- [ ] **Step 7: Compilar y correr toda la suite**

```bash
./mvnw test -q
```
Esperado: `BUILD SUCCESS`. Errores de compilación = campos de entidad usados que no están
en el record del puerto → agregar los campos faltantes al record en los archivos de port.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/veloservice/ordenes/application/port/ \
        src/main/java/com/veloservice/ordenes/infraestructure/adapter/ \
        src/main/java/com/veloservice/ordenes/application/usecase/OrdenService.java \
        src/test/java/com/veloservice/ordenes/application/usecase/OrdenServicePortTest.java
git commit -m "refactor(ordenes): introduce UsuarioPort/BicicletaPort to break infra coupling"
```

**Nota:** Repetir el mismo patrón (puerto + adaptador) para `ServicioRepository`, `ProductoRepository`,
y `MovimientoStockRepository` en commits separados siguiendo exactamente los mismos pasos.

---

## Task 4: Mover enums a sus módulos propietarios (P3)

`config/enums/` tiene 14 enums que pertenecen a módulos específicos. Tenerlos en un paquete
global impide extraer módulos independientemente.

**Mapa de propietarios:**

| Enum | Módulo propietario | Subpaquete destino |
|------|--------------------|--------------------|
| `EstadoOrdenEnum` | ordenes | `ordenes/domain/` |
| `TipoOrdenEnum` + `TipoOrdenEnumConverter` | ordenes | `ordenes/domain/` |
| `PrioridadOrdenEnum` | ordenes | `ordenes/domain/` |
| `EtapaMultimediaEnum` | ordenes | `ordenes/domain/` |
| `EstadoGarantiaEnum` | ordenes | `ordenes/domain/` |
| `EstadoCobroEnum` | finanzas | `finanzas/domain/` |
| `MetodoPagoEnum` | finanzas | `finanzas/domain/` |
| `EstadoSIIEnum` | finanzas | `finanzas/domain/` |
| `TipoDocumentoEnum` | finanzas | `finanzas/domain/` |
| `EstadoCompraEnum` | proveedores_compras | `proveedores_compras/domain/` |
| `TipoMovimientoEnum` | inventario | `inventario/domain/` |
| `TipoArchivoEnum` | ordenes | `ordenes/domain/` |
| `CanalNotificacionEnum` | notificaciones | `notificaciones/domain/` |
| `TipoNotificacionEnum` | notificaciones | `notificaciones/domain/` |
| `EstadoNotificacionEnum` | notificaciones | `notificaciones/domain/` |
| `PlanSaasEnum` | administracion | `administracion/domain/` |

**Files:** Los 14 enums + todos los archivos que los importen.

- [ ] **Step 1: Ejecutar migración por módulo — empezar con `ordenes`**

Para cada enum de `ordenes`:

```bash
# Ver todos los importadores del enum a mover
grep -rln "import com.veloservice.config.enums.EstadoOrdenEnum" src/
```

1. Crear el archivo en el destino con el paquete correcto.
2. Actualizar todos los importadores.
3. Eliminar el archivo original de `config/enums/`.

Ejemplo para `EstadoOrdenEnum`:

Nuevo archivo `src/main/java/com/veloservice/ordenes/domain/EstadoOrdenEnum.java`:
```java
package com.veloservice.ordenes.domain;

public enum EstadoOrdenEnum {
    recibida,
    en_diagnostico,
    esperando_repuestos,
    en_reparacion,
    control_calidad,
    lista_para_entrega,
    entregada,
    cancelada
}
```

Luego reemplazar todos los imports:
```bash
find src/ -name "*.java" -exec sed -i \
  's/import com.veloservice.config.enums.EstadoOrdenEnum;/import com.veloservice.ordenes.domain.EstadoOrdenEnum;/g' {} \;
```

- [ ] **Step 2: Compilar tras cada enum migrado**

```bash
./mvnw compile -q
```
Si hay errores = import no reemplazado. Buscar con `grep -rn "config.enums.EstadoOrdenEnum" src/`.

- [ ] **Step 3: Repetir para los otros 13 enums**

Mismo proceso (crear → reemplazar imports → eliminar original → compilar) para cada enum
en el orden del mapa de propietarios de arriba.

`TipoOrdenEnumConverter` (converter JPA) va junto con `TipoOrdenEnum` al mismo paquete.

- [ ] **Step 4: Verificar que config/enums/ queda vacío y eliminar el directorio**

```bash
ls src/main/java/com/veloservice/config/enums/
rmdir src/main/java/com/veloservice/config/enums/
```

- [ ] **Step 5: Correr toda la suite**

```bash
./mvnw test -q
```
Esperado: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor(config): move business enums to their owning modules"
```

---

## Task 5: Separar DTOs de controllers en subpaquete dto/ (P4)

`interfaces/rest/` mezcla controllers con clases de request/response. Al escalar, cada
controller crece con sus DTOs y el paquete se vuelve imposible de navegar. La solución
es mover todas las clases `*Request` y `*Response` a `interfaces/rest/dto/`.

Esta tarea aplica la separación al módulo `ordenes` (el más grande, 20 archivos en rest/).
Usar como plantilla para los demás módulos.

**Files:**
- Move: todos los `*Request.java` y `*Response.java` de `ordenes/interfaces/rest/`
  → `ordenes/interfaces/rest/dto/`
- Modify: todos los archivos que importen esas clases

- [ ] **Step 1: Identificar los archivos a mover en ordenes**

```bash
ls src/main/java/com/veloservice/ordenes/interfaces/rest/ | grep -v Controller
```

Resultado esperado: `ComentarioRequest`, `ComentarioResponse`, `DashboardAlertasResponse`,
`DashboardHoyResponse`, `EstadoChangeRequest`, `GarantiaRequest`, `GarantiaResponse`,
`MultimediaRequest`, `MultimediaResponse`, `NuevaOrdenRequest`, `OrdenActividadRecienteResponse`,
`OrdenListaEntregaResponse`, `OrdenMetricasResponse`, `OrdenProductoRequest`, `OrdenProductoResponse`,
`OrdenRequest`, `OrdenResponse`, `OrdenResumenResponse`, `OrdenServicioRequest`, `OrdenUrgenteResponse`.

- [ ] **Step 2: Crear el directorio `dto/` y mover los archivos**

Para cada archivo no-controller:

1. Crear en `src/main/java/com/veloservice/ordenes/interfaces/rest/dto/` con el paquete
   cambiado a `com.veloservice.ordenes.interfaces.rest.dto`.
2. Actualizar todos sus importadores.

Ejemplo para `OrdenResponse.java`:
```bash
# Encontrar importadores
grep -rln "import com.veloservice.ordenes.interfaces.rest.OrdenResponse" src/

# Reemplazar import en todos
find src/ -name "*.java" -exec sed -i \
  's/import com.veloservice.ordenes.interfaces.rest.OrdenResponse;/import com.veloservice.ordenes.interfaces.rest.dto.OrdenResponse;/g' {} \;
```

Repetir para cada archivo identificado en Step 1.

- [ ] **Step 3: Eliminar archivos originales del paquete rest/**

```bash
ls src/main/java/com/veloservice/ordenes/interfaces/rest/ | grep -v "\.java"
# solo deben quedar los *Controller.java
```

- [ ] **Step 4: Compilar y correr tests**

```bash
./mvnw test -q
```

- [ ] **Step 5: Repetir para los demás módulos**

Aplicar el mismo proceso a:
- `administracion/interfaces/rest/` (mover `Auth*Request/Response`, `Mecanico*Request/Response`)
- `clientes/interfaces/rest/`
- `inventario/interfaces/rest/`
- `finanzas/interfaces/rest/`
- `proveedores_compras/interfaces/rest/`
- `servicios/interfaces/rest/`
- `notificaciones/interfaces/rest/`

Cada módulo en un commit separado.

- [ ] **Step 6: Commit por módulo**

```bash
git add src/main/java/com/veloservice/ordenes/interfaces/rest/dto/ \
        src/main/java/com/veloservice/ordenes/interfaces/rest/*Controller.java \
        src/ # mappers y otros importadores actualizados
git commit -m "refactor(ordenes): move request/response DTOs to interfaces/rest/dto/"
```

---

## Checklist de cobertura

| Requisito | Tarea que lo implementa |
|-----------|------------------------|
| Controller no accede a repository directamente | Task 1 |
| ThreadLocal contexts en paquete correcto | Task 2 |
| Módulo ordenes no importa infra de otros módulos | Task 3 |
| Enums en módulos propietarios | Task 4 |
| Controllers limpios, sin DTOs mezclados | Task 5 |

## Lo que este plan NO aborda (backlog)

- **`OrdenService` god class (518 líneas):** Extraer `OrdenProductoService` ya existe como
  `MultimediaService`. El siguiente paso natural es extraer `OrdenEstadoService` con la máquina
  de estados. Hacer después de Task 3 cuando los puertos estén definidos.
- **CSV export en controller:** Mover lógica de `exportarCsv()` a un `OrdenExportService`.
- **`listarListaEntrega()` stub:** Implementar o eliminar el endpoint.
- **Cobertura de tests:** Solo 16 tests para 18 controllers + 26 repositories. Priorizar
  tests de controllers y servicios críticos (OrdenService, AuthService).
- **MecanicoMapper duplicado:** En `administracion/interfaces/mapper/` y
  `ordenes/interfaces/mapper/`. Uno de ellos sobra.
