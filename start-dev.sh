#!/bin/bash
set -e

PORT=8080

echo "=========================================="
echo "  VELOSERVICE - MODO DESARROLLO LOCAL"
echo "=========================================="

# 1. Encender Docker PostgreSQL
echo "[1/6] Verificando Docker..."
if ! docker ps | grep -q "postgres-local"; then
    echo "      → Encendiendo postgres-local..."
    docker start postgres-local
    sleep 3
else
    echo "      → postgres-local ya está corriendo"
fi

# 2. Esperar a que PostgreSQL responda
echo "[2/6] Esperando PostgreSQL..."
until psql "postgresql://velo_user:velo_pass@localhost:5433/veloservice_db" -c "SELECT 1;" > /dev/null 2>&1; do
    echo "      → Esperando..."
    sleep 2
done
echo "      → PostgreSQL listo"

# 3. Insertar datos demo (idempotente: no falla si ya existen)
echo "[3/6] Insertando datos demo..."
psql "postgresql://velo_user:velo_pass@localhost:5433/veloservice_db" > /dev/null 2>&1 << 'SQLEOF'
INSERT INTO sucursales (id, taller_id, nombre, direccion, telefono, email, activo, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'Sucursal Centro', 'Av. Principal 123', '+56912345678', 'centro@veloservice.cl', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (id, nombre, descripcion, activo)
VALUES ('770e8400-e29b-41d4-a716-446655440002', 'ADMIN_SUCURSAL', 'Administrador de sucursal', true)
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuarios (id, sucursal_id, rol_id, nombre, apellido, rut, email, telefono, password_hash, activo, last_login, created_at)
VALUES ('880e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', 'Oscar', 'Muñoz', '12.345.678-9', 'admin@veloservice.cl', '+56998765432', '$2b$10$instik2q6t5EwB075oyTT.VP99/SaUbdTsAp.xJjpKsyBWUX8Q1hm', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO clientes (id, nombre, apellido, rut, telefono, email, direccion, created_at, updated_at)
VALUES ('990e8400-e29b-41d4-a716-446655440004', 'Juan', 'Pérez', '11.111.111-1', '+56911111111', 'juan@email.com', 'Calle Falsa 123', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO bicicletas (id, cliente_id, marca, modelo, tipo, aro, color, numero_serie, anio, foto_url, notas, created_at)
VALUES ('aa0e8400-e29b-41d4-a716-446655440005', '990e8400-e29b-41d4-a716-446655440004', 'Trek', 'Marlin 7', 'MTB', '29', 'Rojo', 'SN123456789', 2023, NULL, 'Bicicleta de demo', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO servicios (id, nombre, descripcion, precio_base, es_garantia, activo, created_at)
VALUES ('cc0e8400-e29b-41d4-a716-446655440007', 'Cambio de frenos', 'Ajuste y cambio de pastillas de freno', 25000.00, false, true, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO productos (id, sucursal_id, categoria_id, nombre, sku, marca, unidad_medida, precio_costo, precio_venta, stock, stock_minimo, activo, created_at, updated_at)
VALUES ('bb0e8400-e29b-41d4-a716-446655440006', '660e8400-e29b-41d4-a716-446655440001', NULL, 'Pastilla de freno Shimano', 'SKU-001', 'Shimano', 'unidad', 15000.00, 25000.00, 10, 2, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
SQLEOF
echo "      → Datos listos"

# 4. Asegurar Java 21 antes de compilar
echo "[4/7] Verificando Java 21..."
for candidate in \
    ${JAVA_HOME:-} \
    /usr/lib/jvm/java-21-openjdk-amd64 \
    /usr/lib/jvm/java-1.21.0-openjdk-amd64 \
    /usr/local/sdkman/candidates/java/21.0.10-ms \
    "$HOME/.sdkman/candidates/java/current"; do
    if [ -n "$candidate" ] && [ -x "$candidate/bin/java" ] && "$candidate/bin/java" -version 2>&1 | grep -q '"21\.'; then
        export JAVA_HOME="$candidate"
        export PATH="$JAVA_HOME/bin:$PATH"
        break
    fi
done

if ! java -version 2>&1 | grep -q '"21\.'; then
    echo "      ✗ Java 21 no está disponible en este entorno"
    echo "        Instala o activa JDK 21 antes de compilar el proyecto"
    exit 1
fi
echo "      → Java 21 listo"

# 5. Compilar
echo "[5/7] Compilando backend..."
cd /workspaces/veloservice-backend
./mvnw clean compile -q
echo "      → Compilación OK"

# 6. Verificar puerto del backend
echo "[6/7] Verificando puerto ${PORT}..."
PORT_PID="$(lsof -ti tcp:${PORT} -sTCP:LISTEN 2>/dev/null || true)"
if [ -n "$PORT_PID" ]; then
    PORT_CMD="$(ps -p "$PORT_PID" -o args= 2>/dev/null || true)"
    if echo "$PORT_CMD" | grep -Eq "veloservice-backend|spring-boot:run|BikeshopManagerApplication"; then
        echo "      → Instancia previa detectada (PID ${PORT_PID}), deteniendo..."
        kill "$PORT_PID" || true
        sleep 2
    else
        echo "      ✗ Puerto ${PORT} en uso por otro proceso (PID ${PORT_PID})"
        echo "        Comando: ${PORT_CMD}"
        echo "        Libera el puerto y vuelve a ejecutar este script."
        exit 1
    fi
else
    echo "      → Puerto ${PORT} libre"
fi

# 7. Levantar Spring Boot
echo "[7/7] Levantando Spring Boot (dev)..."
echo "Backend iniciando en http://localhost:${PORT}/api/v1"
echo "=========================================="
echo "  CREDENCIALES DEMO:"
echo "  Email: admin@veloservice.cl"
echo "  Pass:  123456"
echo "=========================================="
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run

