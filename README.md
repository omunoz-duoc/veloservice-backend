# BikeShop Manager API

Backend REST para BikeShop Manager, una plataforma de gestión para talleres de bicicletas. La API cubre autenticación, administración de talleres y sucursales, clientes, bicicletas, órdenes de trabajo, servicios, inventario, proveedores, compras, finanzas, notificaciones y carga de multimedia.

La aplicación está construida con Java 21 y Spring Boot 3.3.0. Expone sus endpoints bajo el prefijo:

```text
http://localhost:8080/api/v1
```

## Stack principal

- Java 21
- Spring Boot Web
- Spring Data JPA
- Spring Security + JWT
- Bean Validation
- Flyway
- PostgreSQL para ambientes persistentes
- H2 en memoria para desarrollo y tests
- Cloudflare R2 / API S3 para multimedia
- Resend para correos de recuperación de contraseña
- Maven Wrapper (`./mvnw`)

## Requisitos

Para desarrollo local necesitas:

- JDK 21
- Git
- Bash, Linux/macOS/WSL recomendado para usar `./mvnw`

Opcional, según el flujo que uses:

- Docker, si quieres construir o ejecutar la imagen del backend
- PostgreSQL, si quieres correr con perfil `prod` o `docker`
- `psql`, si quieres usar el script `start-dev.sh`
- Credenciales de Cloudflare R2 y Resend para probar multimedia y correos reales

## Perfiles disponibles

- `dev`: usa H2 en memoria, crea el esquema automáticamente y carga `src/main/resources/data.sql`.
- `test`: usa H2 para pruebas automatizadas.
- `docker`: usa PostgreSQL sin SSL y valores por defecto pensados para contenedores.
- `prod`: usa PostgreSQL con SSL y variables de entorno obligatorias.

Importante: `src/main/resources/application.yml` activa `prod` por defecto. Para desarrollo local ejecuta siempre con `SPRING_PROFILES_ACTIVE=dev`.

## Levantar en desarrollo local

1. Clona el repositorio y entra al directorio:

```bash
git clone <url-del-repositorio>
cd veloservice-backend
```

2. Exporta el perfil `dev` y variables mínimas para R2. Si no vas a probar carga de archivos, puedes usar valores dummy:

```bash
export SPRING_PROFILES_ACTIVE=dev
export CF_R2_ACCOUNT_ID=dev-account
export CF_R2_ACCESS_KEY_ID=dev-access-key
export CF_R2_SECRET_ACCESS_KEY=dev-secret-key
export CF_R2_BUCKET_NAME=dev-bucket
export CF_R2_PUBLIC_BASE_URL=http://localhost:8080/media
```

3. Levanta la API:

```bash
./mvnw spring-boot:run
```

4. Verifica el estado:

```bash
curl http://localhost:8080/api/v1/health
```

Respuesta esperada:

```json
{"status":"UP"}
```

En perfil `dev`, la consola H2 queda disponible en:

```text
http://localhost:8080/api/v1/h2-console
```

Datos de conexión H2:

- JDBC URL: `jdbc:h2:mem:veloservice`
- Usuario: `sa`
- Password: vacío

## Variables de entorno

Para `dev` con H2 no necesitas configurar base de datos, pero la configuración de R2 se carga como bean de Spring, por lo que deben existir estas variables:

```bash
CF_R2_ACCOUNT_ID=
CF_R2_ACCESS_KEY_ID=
CF_R2_SECRET_ACCESS_KEY=
CF_R2_BUCKET_NAME=
CF_R2_PUBLIC_BASE_URL=
```

Para `prod` necesitas, además:

```bash
DB_HOST=
DB_PORT=
DB_NAME=
DB_USERNAME=
DB_PASSWORD=
JWT_SECRET=
RESET_PASSWORD_URL=
```

Variables opcionales:

```bash
RESEND_API_KEY=
RESEND_FROM=
MEDIA_STORAGE_ROOT=/tmp/veloservice-media
MEDIA_PUBLIC_BASE_URL=https://media.veloservice.cl
ADMIN_PLATAFORMA_EMAIL=
ADMIN_PLATAFORMA_PASSWORD=
ADMIN_PLATAFORMA_NOMBRE=
ADMIN_PLATAFORMA_APELLIDO=
```

`application-prod.yml` importa automáticamente un archivo `.env` local si existe:

```text
.env
```

No subas credenciales reales al repositorio.

## Ejecutar tests

Las pruebas del proyecto deben ejecutarse desde la rama `develop`:

```bash
git checkout develop
```

```bash
./mvnw test
```

## Compilar el proyecto

```bash
./mvnw clean package
```

El artefacto queda en:

```text
target/manager-1.0.0.jar
```

Para ejecutarlo manualmente:

```bash
SPRING_PROFILES_ACTIVE=dev \
CF_R2_ACCOUNT_ID=dev-account \
CF_R2_ACCESS_KEY_ID=dev-access-key \
CF_R2_SECRET_ACCESS_KEY=dev-secret-key \
CF_R2_BUCKET_NAME=dev-bucket \
CF_R2_PUBLIC_BASE_URL=http://localhost:8080/media \
java -jar target/manager-1.0.0.jar
```

## Ejecutar con Docker

Construye la imagen:

```bash
docker build -t bikeshop-manager-api .
```

Ejecuta la imagen apuntando a una base PostgreSQL existente:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=veloservice_db \
  -e DB_USERNAME=velo_user \
  -e DB_PASSWORD=velo_pass \
  -e JWT_SECRET=dev-docker-secret-not-for-production-use-min32chars \
  -e CF_R2_ACCOUNT_ID=dev-account \
  -e CF_R2_ACCESS_KEY_ID=dev-access-key \
  -e CF_R2_SECRET_ACCESS_KEY=dev-secret-key \
  -e CF_R2_BUCKET_NAME=dev-bucket \
  -e CF_R2_PUBLIC_BASE_URL=http://localhost:8080/media \
  bikeshop-manager-api
```

En Linux puede ser necesario agregar:

```bash
--add-host=host.docker.internal:host-gateway
```

## Script de desarrollo

El repositorio incluye `start-dev.sh`, que intenta iniciar un contenedor Docker llamado `postgres-local`, cargar datos demo con `psql` y luego ejecutar Spring Boot.

Úsalo solo si tu entorno ya tiene ese contenedor y la ruta esperada por el script:

```bash
./start-dev.sh
```

Para un entorno local limpio, el flujo recomendado es usar el perfil `dev` con H2.

## Documentación adicional

- `docs/endpoint-documentation.md`: ejemplos de endpoints.
- `docs/crear-orden-ejemplos.md`: ejemplos para crear órdenes.
- `docs/orden-create-flow.md`: flujo de creación de órdenes.

## Estructura relevante

```text
src/main/java/com/veloservice
├── administracion
├── auth
├── clientes
├── finanzas
├── inventario
├── notificaciones
├── ordenes
├── proveedores_compras
├── servicios
├── shared
└── config
```

Las migraciones Flyway están en:

```text
src/main/resources/db/migration
```
