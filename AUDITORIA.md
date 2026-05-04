# AUDITORIA VELOSERVICE BACKEND
Fecha: Mon May  4 18:37:09 UTC 2026

## 1. CONFIGURACION YML
### application.yml (EXISTE)
```yaml
spring:
  application:
    name: bikeshop-manager
  datasource:
    url: jdbc:postgresql://localhost:5433/veloservice_db
    username: velo_user
    password: velo_pass
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 20000
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: true
  flyway:
    enabled: false
    locations: classpath:db/migration
    baseline-on-migrate: true
server:
  port: 8080
  servlet:
    context-path: /api/v1
jwt:
  secret: ${JWT_SECRET:cambiaEstaClaveEnProduccionPorUnaMuyLargaDe32CharsMin}
  expiration: 86400000
logging:
  level:
    org.springframework.security: DEBUG
    com.bikeshop.manager: DEBUG
```

### application-dev.yml (NO EXISTE)

### application-prod.yml (NO EXISTE)

## 2. ESTRUCTURA DE PAQUETES
/workspaces/veloservice-backend/src/main/java
├── cl
│   └── veloservice
│       └── backend
└── com
    ├── bikeshop
    │   └── manager
    │       ├── application
    │       ├── config
    │       ├── domain
    │       ├── infrastructure
    │       └── interface_
    └── veloservice
        ├── administracion
        │   ├── api
        │   ├── internal
        │   └── web
        ├── catalogo
        │   ├── api
        │   ├── internal
        │   └── web
        ├── config
        │   ├── enums
        │   ├── security
        │   ├── tenant
        │   └── web
        ├── crm
        │   ├── api
        │   ├── internal
        │   └── web
        ├── finanzas
        │   ├── api
        │   ├── internal
        │   └── web
        ├── inventario
        │   ├── api
        │   ├── internal
        │   └── web
        ├── notificaciones
        │   ├── internal
        │   └── web
        └── taller
            ├── api
            ├── internal
            └── web

45 directories

## 3. ENTIDADES JPA (@Entity)
- **Cobro** | pkg: `com.veloservice.finanzas.internal.entity` | tabla: "cobros" | @Id: 1 | auditoria: 2 | enums: 4 | relaciones: []
- **Servicio** | pkg: `com.veloservice.catalogo.internal.entity` | tabla: "servicios" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: []
- **Producto** | pkg: `com.veloservice.catalogo.internal.entity` | tabla: "productos" | @Id: 1 | auditoria: 4 | enums: 0 | relaciones: []
- **CategoriaProducto** | pkg: `com.veloservice.catalogo.internal.entity` | tabla: "categorias_producto" | @Id: 1 | auditoria: 0 | enums: 0 | relaciones: []
- **SucursalServicio** | pkg: `com.veloservice.catalogo.internal.entity` | tabla: "sucursal_servicios" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: []
- **Notificacion** | pkg: `com.veloservice.notificaciones.internal.entity` | tabla: "notificaciones" | @Id: 1 | auditoria: 2 | enums: 3 | relaciones: []
- **Cliente** | pkg: `com.veloservice.crm.internal.entity` | tabla: "clientes" | @Id: 1 | auditoria: 4 | enums: 0 | relaciones: [@OneToMany]
- **SucursalCliente** | pkg: `com.veloservice.crm.internal.entity` | tabla: "sucursal_clientes" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: []
- **Membresia** | pkg: `com.veloservice.crm.internal.entity` | tabla: "membresias" | @Id: 1 | auditoria: 0 | enums: 0 | relaciones: []
- **Bicicleta** | pkg: `com.veloservice.crm.internal.entity` | tabla: "bicicletas" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: [@ManyToOne]
- **Sucursal** | pkg: `com.veloservice.administracion.internal.entity` | tabla: "sucursales" | @Id: 1 | auditoria: 4 | enums: 0 | relaciones: [@ManyToOne]
- **Usuario** | pkg: `com.veloservice.administracion.internal.entity` | tabla: "usuarios" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: [@ManyToOne]
- **Taller** | pkg: `com.veloservice.administracion.internal.entity` | tabla: "talleres" | @Id: 1 | auditoria: 4 | enums: 1 | relaciones: []
- **RolPermiso** | pkg: `com.veloservice.administracion.internal.entity` | tabla: "rol_permisos" | @Id: 1 | auditoria: 0 | enums: 0 | relaciones: [@ManyToOne]
- **Modulo** | pkg: `com.veloservice.administracion.internal.entity` | tabla: "modulos" | @Id: 1 | auditoria: 0 | enums: 0 | relaciones: []
- **Rol** | pkg: `com.veloservice.administracion.internal.entity` | tabla: "roles" | @Id: 1 | auditoria: 0 | enums: 0 | relaciones: []
- **OrdenEstado** | pkg: `com.veloservice.taller.internal.entity` | tabla: "orden_estados" | @Id: 1 | auditoria: 2 | enums: 2 | relaciones: []
- **Garantia** | pkg: `com.veloservice.taller.internal.entity` | tabla: "garantias" | @Id: 1 | auditoria: 4 | enums: 1 | relaciones: []
- **OrdenServicio** | pkg: `com.veloservice.taller.internal.entity` | tabla: "orden_servicios" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: []
- **OrdenProducto** | pkg: `com.veloservice.taller.internal.entity` | tabla: "orden_productos" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: []
- **Orden** | pkg: `com.veloservice.taller.internal.entity` | tabla: "ordenes" | @Id: 1 | auditoria: 4 | enums: 2 | relaciones: []
- **Multimedia** | pkg: `com.veloservice.taller.internal.entity` | tabla: "multimedia" | @Id: 1 | auditoria: 2 | enums: 2 | relaciones: []
- **SucursalProveedor** | pkg: `com.veloservice.inventario.internal.entity` | tabla: "sucursal_proveedores" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: []
- **Proveedor** | pkg: `com.veloservice.inventario.internal.entity` | tabla: "proveedores" | @Id: 1 | auditoria: 2 | enums: 0 | relaciones: []
- **MovimientoStock** | pkg: `com.veloservice.inventario.internal.entity` | tabla: "movimientos_stock" | @Id: 1 | auditoria: 2 | enums: 1 | relaciones: []
- **Compra** | pkg: `com.veloservice.inventario.internal.entity` | tabla: "compras" | @Id: 1 | auditoria: 2 | enums: 1 | relaciones: [@OneToMany]
- **CompraProducto** | pkg: `com.veloservice.inventario.internal.entity` | tabla: "compra_productos" | @Id: 1 | auditoria: 0 | enums: 0 | relaciones: [@ManyToOne]

## 4. REPOSITORIES
- **CobroRepository** | tipo: interface | extends JpaRepository<Cobro, | Impl: 0
- **ServicioRepository** | tipo: interface | extends JpaRepository<Servicio, | Impl: 0
- **ProductoRepository** | tipo: interface | extends JpaRepository<Producto, | Impl: 0
- **SucursalServicioRepository** | tipo: interface | extends JpaRepository<SucursalServicio, | Impl: 0
- **NotificacionRepository** | tipo: interface | extends JpaRepository<Notificacion, | Impl: 0
- **BicicletaRepository** | tipo: interface | extends JpaRepository<Bicicleta, | Impl: 0
- **SucursalClienteRepository** | tipo: interface | extends JpaRepository<SucursalCliente, | Impl: 0
- **MembresiaRepository** | tipo: interface | extends JpaRepository<Membresia, | Impl: 0
- **ClienteRepository** | tipo: interface | extends JpaRepository<Cliente, | Impl: 0
- **RolRepository** | tipo: interface | extends JpaRepository<Rol, | Impl: 0
- **SucursalRepository** | tipo: interface | extends JpaRepository<Sucursal, | Impl: 0
- **UsuarioRepository** | tipo: interface | extends JpaRepository<Usuario, | Impl: 0
- **TallerRepository** | tipo: interface | extends JpaRepository<Taller, | Impl: 0
- **OrdenEstadoRepository** | tipo: interface | extends JpaRepository<OrdenEstado, | Impl: 0
- **OrdenRepository** | tipo: interface | extends JpaRepository<Orden, | Impl: 0
- **GarantiaRepository** | tipo: interface | extends JpaRepository<Garantia, | Impl: 0
- **OrdenServicioRepository** | tipo: interface | extends JpaRepository<OrdenServicio, | Impl: 0
- **OrdenProductoRepository** | tipo: interface | extends JpaRepository<OrdenProducto, | Impl: 0
- **MultimediaRepository** | tipo: interface | extends JpaRepository<Multimedia, | Impl: 0
- **MovimientoStockRepository** | tipo: interface | extends JpaRepository<MovimientoStock, | Impl: 0
- **ProveedorRepository** | tipo: interface | extends JpaRepository<Proveedor, | Impl: 0
- **SucursalProveedorRepository** | tipo: interface | extends JpaRepository<SucursalProveedor, | Impl: 0
- **CompraRepository** | tipo: interface | extends JpaRepository<Compra, | Impl: 0

## 5. SEGURIDAD JWT
### JwtTokenProvider claims
```java
    public String generateToken(UUID userId, String email, String rol, UUID sucursalId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("rol", rol)
                .claim("sucursalId", sucursalId.toString())
                .claim("tallerId", fixedTallerId.toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(jwtSecret, Jwts.SIG.HS256);
        return builder.compact();
    }

    /**
     * Extracts token claims.
     *
     * @param token JWT string
     * @return token claims
     */
    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Validates the JWT signature and expiration.
     *
     * @param token JWT string
     * @return true if the token is valid
```
### JwtAuthenticationFilter
```java
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                UUID userId = tokenProvider.getUserId(jwt);
                String rol = tokenProvider.getRol(jwt);
                UUID sucursalId = tokenProvider.getSucursalId(jwt);

                UserDetails userDetails = User.builder()
                        .username(userId.toString())
                        .password("")
                        .authorities(Collections.emptyList())
                        .build();

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, Collections.singletonList(() -> "ROLE_" + rol));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
                SucursalContext.setCurrentSucursal(sucursalId);
                UsuarioContext.setCurrentUser(userId);
            }
        } catch (Exception e) {
            log.error("Error procesando JWT", e);
        }
        filterChain.doFilter(request, response);
    }
```
### TenantOperationAspect SQL
```java
            entityManager.createNativeQuery("SELECT set_config('request.jwt.claim.sucursal_id', ?, true)")
                .setParameter(1, tenantId.toString())
                    .getSingleResult();
        }
        return joinPoint.proceed();
    }
}
```
### AuthController endpoints
@PostMapping("/login"
@RequestMapping("/auth"

### AuthController setup SaaS?
23: * Authentication endpoints for login and initial setup.

## 6. FLYWAY MIGRATIONS
- **V1__init_platform.sql** | tablas: CREATE TABLE roles,CREATE TABLE talleres,CREATE TABLE usuarios
- **V2__crm_tenant.sql** | tablas: CREATE TABLE bicicletas,CREATE TABLE clientes,CREATE TABLE taller_clientes
- **V3__ordenes_trabajo.sql** | tablas: CREATE TABLE multimedia,CREATE TABLE orden_estados,CREATE TABLE ordenes
- **V4__inventario.sql** | tablas: CREATE TABLE categorias_producto,CREATE TABLE compra_productos,CREATE TABLE compras,CREATE TABLE movimientos_stock,CREATE TABLE productos,CREATE TABLE proveedores
- **V5__cobros_membresias_garantias.sql** | tablas: CREATE TABLE cobros,CREATE TABLE garantias,CREATE TABLE membresias,CREATE TABLE orden_productos,CREATE TABLE orden_servicios,CREATE TABLE servicios
- **V6__add_created_at_membresias.sql** | tablas: 
- **mer_bikeshop_schema.sql** | tablas: CREATE TABLE bicicletas,CREATE TABLE categorias_producto,CREATE TABLE clientes,CREATE TABLE cobros,CREATE TABLE compra_productos,CREATE TABLE compras,CREATE TABLE garantias,CREATE TABLE membresias,CREATE TABLE modulos,CREATE TABLE movimientos_stock,CREATE TABLE multimedia,CREATE TABLE notificaciones,CREATE TABLE orden_estados,CREATE TABLE orden_productos,CREATE TABLE orden_servicios,CREATE TABLE ordenes,CREATE TABLE productos,CREATE TABLE proveedores,CREATE TABLE rol_permisos,CREATE TABLE roles,CREATE TABLE servicios,CREATE TABLE sucursal_clientes,CREATE TABLE sucursal_proveedores,CREATE TABLE sucursal_servicios,CREATE TABLE sucursales,CREATE TABLE talleres,CREATE TABLE usuarios

## 7. ENUMS JAVA
- EstadoSIIEnum (com.veloservice.config.enums)
- TipoNotificacionEnum (com.veloservice.config.enums)
- TipoDocumentoEnum (com.veloservice.config.enums)
- CanalNotificacionEnum (com.veloservice.config.enums)
- EtapaMultimediaEnum (com.veloservice.config.enums)
- EstadoCobroEnum (com.veloservice.config.enums)
- EstadoCompraEnum (com.veloservice.config.enums)
- EstadoGarantiaEnum (com.veloservice.config.enums)
- EstadoNotificacionEnum (com.veloservice.config.enums)
- TipoOrdenEnum (com.veloservice.config.enums)
- PlanSaasEnum (com.veloservice.config.enums)
- MetodoPagoEnum (com.veloservice.config.enums)
- TipoMovimientoEnum (com.veloservice.config.enums)
- EstadoOrdenEnum (com.veloservice.config.enums)
- TipoArchivoEnum (com.veloservice.config.enums)

## 8. CONTROLLERS (@RestController)
- **CobroController** | base: @RequestMapping("/cobros" | usa DTOs: 0 | devuelve entidades: 2
- **ServicioController** | base: @RequestMapping("/servicios" | usa DTOs: 0 | devuelve entidades: 6
- **ProductoController** | base: @RequestMapping("/productos" | usa DTOs: 0 | devuelve entidades: 3
- **NotificacionController** | base: @RequestMapping("/notificaciones" | usa DTOs: 0 | devuelve entidades: 3
- **GlobalExceptionHandler** | base:  | usa DTOs: 0 | devuelve entidades: 1
- **ClienteController** | base: @RequestMapping("/clientes" | usa DTOs: 0 | devuelve entidades: 3
- **BicicletaController** | base: @RequestMapping("/bicicletas" | usa DTOs: 0 | devuelve entidades: 3
- **HealthController** | base:  | usa DTOs: 0 | devuelve entidades: 1
- **AuthController** | base: @RequestMapping("/auth" | usa DTOs: 0 | devuelve entidades: 1
- **GarantiaController** | base: @RequestMapping("/garantias" | usa DTOs: 0 | devuelve entidades: 2
- **DashboardController** | base: @RequestMapping("/dashboard" | usa DTOs: 0 | devuelve entidades: 3
- **MultimediaController** | base: @RequestMapping("/multimedia" | usa DTOs: 0 | devuelve entidades: 2
- **OrdenController** | base: @RequestMapping("/ordenes" | usa DTOs: 0 | devuelve entidades: 6
- **ProveedorController** | base: @RequestMapping("/proveedores" | usa DTOs: 0 | devuelve entidades: 3
- **CompraController** | base: @RequestMapping("/compras" | usa DTOs: 0 | devuelve entidades: 3

## 9. POSIBLE BASURA / OBSOLETO
- **AplicarMembresiaRequest.java** | referencias externas: 0
- **MembresiaRequest.java** | referencias externas: 0

## 10. POM.XML (dependencias clave)
```xml
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.0</version>
--
        <jjwt.version>0.12.5</jjwt.version>
        <flyway.version>10.21.0</flyway.version>
        <lombok.version>1.18.34</lombok.version>
    </properties>
--
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId><version>${flyway.version}</version></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-database-postgresql</artifactId><version>${flyway.version}</version></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>${jjwt.version}</version></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>${jjwt.version}</version><scope>runtime</scope></dependency>
        <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
    </dependencies>
--
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
--
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
```

--- FIN AUDITORIA ---
