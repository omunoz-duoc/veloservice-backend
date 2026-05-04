#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost:8080/api/v1}
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5433}
DB_NAME=${DB_NAME:-veloservice_db}
DB_USER=${DB_USER:-velo_user}
DB_PASS=${DB_PASS:-velo_pass}

ROLE_ID="11111111-1111-1111-1111-111111111111"
SUCURSAL_ID="22222222-2222-2222-2222-222222222222"
USER_ID="33333333-3333-3333-3333-333333333333"
TALLER_ID="550e8400-e29b-41d4-a716-446655440000"

echo "[1/6] Checking API health..."
if ! curl -sf "$BASE_URL/health" >/dev/null; then
  echo "API not reachable at $BASE_URL. Start the app and try again."
  exit 1
fi

echo "[2/6] Seeding admin user in DB..."
PGPASSWORD="$DB_PASS" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -v ON_ERROR_STOP=1 <<SQL
INSERT INTO talleres (id, nombre, rut, plan_saas, activo, created_at, updated_at)
VALUES ('$TALLER_ID', 'VeloService Demo', '76.123.456-7', 'basico', TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO roles (id, nombre, descripcion, activo)
VALUES ('$ROLE_ID', 'ADMIN', 'Administrador', TRUE)
ON CONFLICT (nombre) DO UPDATE SET activo = EXCLUDED.activo;

INSERT INTO sucursales (id, taller_id, nombre, direccion, telefono, email, activo, created_at, updated_at)
VALUES ('$SUCURSAL_ID', '$TALLER_ID', 'Casa Central', 'Av Demo 123', '+56 9 0000 0000', 'sucursal@veloservice.cl', TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO usuarios (id, sucursal_id, rol_id, nombre, apellido, rut, email, telefono, password_hash, activo)
VALUES (
  '$USER_ID',
  '$SUCURSAL_ID',
  '$ROLE_ID',
  'Admin',
  'Demo',
  '1-9',
  'admin@veloservice.cl',
  '+56 9 0000 0000',
  crypt('123456', gen_salt('bf', 12)),
  TRUE
)
ON CONFLICT (email) DO UPDATE SET
  password_hash = EXCLUDED.password_hash,
  rol_id = EXCLUDED.rol_id,
  sucursal_id = EXCLUDED.sucursal_id,
  activo = TRUE;
SQL

echo "[3/6] Logging in..."
LOGIN_JSON=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@veloservice.cl","password":"123456"}')

TOKEN=$(python3 - <<'PY'
import json, sys
try:
    data = json.load(sys.stdin)
    print(data.get("token", ""))
except Exception:
    print("")
PY
<<< "$LOGIN_JSON")

if [[ -z "$TOKEN" ]]; then
  echo "Login failed. Response: $LOGIN_JSON"
  exit 1
fi

echo "TOKEN OK"

AUTH_HEADER=("-H" "Authorization: Bearer $TOKEN")
JSON_HEADER=("-H" "Content-Type: application/json")

echo "[4/6] Creating cliente..."
CLIENTE_JSON=$(curl -s -X POST "$BASE_URL/clientes" "${AUTH_HEADER[@]}" "${JSON_HEADER[@]}" \
  -d '{
    "nombre": "Ana",
    "apellido": "Perez",
    "rut": "12345678-9",
    "telefono": "+56 9 1234 5678",
    "email": "ana@dominio.cl",
    "direccion": "Av. Siempre Viva 123"
  }')

CLIENTE_ID=$(python3 - <<'PY'
import json, sys
try:
    data = json.load(sys.stdin)
    print(data.get("id", ""))
except Exception:
    print("")
PY
<<< "$CLIENTE_JSON")

if [[ -z "$CLIENTE_ID" ]]; then
  echo "Cliente creation failed. Response: $CLIENTE_JSON"
  exit 1
fi

echo "CLIENTE_ID=$CLIENTE_ID"

echo "[5/6] Creating bicicleta..."
BICI_JSON=$(curl -s -X POST "$BASE_URL/bicicletas/cliente/$CLIENTE_ID" "${AUTH_HEADER[@]}" "${JSON_HEADER[@]}" \
  -d '{
    "marca": "Trek",
    "modelo": "Domane",
    "tipo": "ruta",
    "aro": "700c",
    "color": "rojo"
  }')

BICI_ID=$(python3 - <<'PY'
import json, sys
try:
    data = json.load(sys.stdin)
    print(data.get("id", ""))
except Exception:
    print("")
PY
<<< "$BICI_JSON")

if [[ -z "$BICI_ID" ]]; then
  echo "Bicicleta creation failed. Response: $BICI_JSON"
  exit 1
fi

echo "BICI_ID=$BICI_ID"

echo "[6/6] Creating orden..."
ORDEN_JSON=$(curl -s -X POST "$BASE_URL/ordenes" "${AUTH_HEADER[@]}" "${JSON_HEADER[@]}" \
  -d "{\n    \"bicicletaId\": \"$BICI_ID\",\n    \"tipo\": \"reparacion\",\n    \"diagnosticoInicial\": \"ruido en transmision\",\n    \"observacionesCliente\": \"urgente\",\n    \"multimedia\": [\n      { \"url\": \"https://img.com/foto1.jpg\", \"tipoArchivo\": \"imagen\", \"descripcion\": \"ruido en cambio\" }\n    ]\n  }")

ORDEN_ID=$(python3 - <<'PY'
import json, sys
try:
    data = json.load(sys.stdin)
    print(data.get("id", ""))
except Exception:
    print("")
PY
<<< "$ORDEN_JSON")

if [[ -z "$ORDEN_ID" ]]; then
  echo "Orden creation failed. Response: $ORDEN_JSON"
  exit 1
fi

echo "ORDEN_ID=$ORDEN_ID"

echo "\nDemo OK. Listados:"
curl -s "$BASE_URL/clientes" "${AUTH_HEADER[@]}"
echo ""
curl -s "$BASE_URL/bicicletas" "${AUTH_HEADER[@]}"
echo ""
curl -s "$BASE_URL/ordenes" "${AUTH_HEADER[@]}"
echo ""
