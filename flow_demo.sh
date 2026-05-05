#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost:8080/api/v1}
EMAIL=${EMAIL:-admin@veloservice.cl}
PASS=${PASS:-123456}
SKU=${SKU:-CAD-11V-$(date +%s)}

log() {
  echo "[$(date +%H:%M:%S)] $*"
}

log "1) Health"
curl -i -sS "$BASE_URL/health"
echo

log "2) Login"
LOGIN_RESPONSE=$(curl -sS -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  --data-binary @- <<JSON
{"email":"$EMAIL","password":"$PASS"}
JSON
)

echo "$LOGIN_RESPONSE"

TOKEN=$(printf '%s' "$LOGIN_RESPONSE" | python3 -c 'import sys,json
try:
    d=json.load(sys.stdin); print(d.get("token",""))
except Exception:
    print("")')

if [[ -z "$TOKEN" ]]; then
  echo "LOGIN FALLO. Revisa el JSON impreso arriba."
  exit 1
fi

AUTH="Authorization: Bearer $TOKEN"
log "TOKEN OK"

echo
log "3) Crear cliente"
CLIENTE_RESPONSE=$(curl -sS -X POST "$BASE_URL/clientes" \
  -H "$AUTH" -H "Content-Type: application/json" \
  --data-binary @- <<JSON
{"nombre":"Ana","apellido":"Perez","rut":"12345678-9","telefono":"+56 9 1234 5678","email":"ana@dominio.cl","direccion":"Av. Siempre Viva 123"}
JSON
)

echo "$CLIENTE_RESPONSE"

CLIENTE_ID=$(printf '%s' "$CLIENTE_RESPONSE" | python3 -c 'import sys,json
try:
    d=json.load(sys.stdin); print(d.get("id",""))
except Exception:
    print("")')

if [[ -z "$CLIENTE_ID" ]]; then
  echo "No se pudo obtener CLIENTE_ID"
  exit 1
fi
log "CLIENTE_ID=$CLIENTE_ID"

echo
log "4) Crear bicicleta"
BICI_RESPONSE=$(curl -sS -X POST "$BASE_URL/bicicletas/cliente/$CLIENTE_ID" \
  -H "$AUTH" -H "Content-Type: application/json" \
  --data-binary @- <<JSON
{"marca":"Trek","modelo":"Domane","tipo":"ruta","aro":"700c","color":"rojo"}
JSON
)

echo "$BICI_RESPONSE"

BICI_ID=$(printf '%s' "$BICI_RESPONSE" | python3 -c 'import sys,json
try:
    d=json.load(sys.stdin); print(d.get("id",""))
except Exception:
    print("")')

if [[ -z "$BICI_ID" ]]; then
  echo "No se pudo obtener BICI_ID"
  exit 1
fi
log "BICI_ID=$BICI_ID"

echo
log "5) Crear producto (SKU unico: $SKU)"
PRODUCTO_RESPONSE=$(curl -sS -X POST "$BASE_URL/productos" \
  -H "$AUTH" -H "Content-Type: application/json" \
  --data-binary @- <<JSON
{"nombre":"Cadena 11v","sku":"$SKU","marca":"Shimano","unidadMedida":"unidad","precioCosto":12000,"precioVenta":19990,"stock":10,"stockMinimo":2}
JSON
)

echo "$PRODUCTO_RESPONSE"

PRODUCTO_ID=$(printf '%s' "$PRODUCTO_RESPONSE" | python3 -c 'import sys,json
try:
    d=json.load(sys.stdin); print(d.get("id",""))
except Exception:
    print("")')

if [[ -z "$PRODUCTO_ID" ]]; then
  echo "No se pudo obtener PRODUCTO_ID"
  exit 1
fi
log "PRODUCTO_ID=$PRODUCTO_ID"

echo
log "6) Crear servicio"
SERVICIO_RESPONSE=$(curl -sS -X POST "$BASE_URL/servicios" \
  -H "$AUTH" -H "Content-Type: application/json" \
  --data-binary @- <<JSON
{"nombre":"Ajuste frenos","descripcion":"Ajuste completo","precioBase":15000,"activo":true}
JSON
)

echo "$SERVICIO_RESPONSE"

SERVICIO_ID=$(printf '%s' "$SERVICIO_RESPONSE" | python3 -c 'import sys,json
try:
    d=json.load(sys.stdin); print(d.get("id",""))
except Exception:
    print("")')

if [[ -z "$SERVICIO_ID" ]]; then
  echo "No se pudo obtener SERVICIO_ID"
  exit 1
fi
log "SERVICIO_ID=$SERVICIO_ID"

echo
log "7) Crear orden"
ORDEN_RESPONSE=$(curl -sS -X POST "$BASE_URL/ordenes" \
  -H "$AUTH" -H "Content-Type: application/json" \
  --data-binary @- <<JSON
{"bicicletaId":"$BICI_ID","tipo":"reparacion","diagnosticoInicial":"ruido en transmision","observacionesCliente":"urgente","multimedia":[{"url":"https://img.com/foto1.jpg","tipoArchivo":"imagen","descripcion":"ruido en cambio"}]}
JSON
)

echo "$ORDEN_RESPONSE"

ORDEN_ID=$(printf '%s' "$ORDEN_RESPONSE" | python3 -c 'import sys,json
try:
    d=json.load(sys.stdin); print(d.get("id",""))
except Exception:
    print("")')

if [[ -z "$ORDEN_ID" ]]; then
  echo "No se pudo obtener ORDEN_ID"
  exit 1
fi
log "ORDEN_ID=$ORDEN_ID"

echo
log "8) Agregar items a orden"
curl -sS -X POST "$BASE_URL/ordenes/$ORDEN_ID/servicios" \
  -H "$AUTH" -H "Content-Type: application/json" \
  --data-binary @- <<JSON
{"servicioId":"$SERVICIO_ID","notas":"incluye limpieza"}
JSON

echo

curl -sS -X POST "$BASE_URL/ordenes/$ORDEN_ID/productos" \
  -H "$AUTH" -H "Content-Type: application/json" \
  --data-binary @- <<JSON
{"productoId":"$PRODUCTO_ID","cantidad":1,"proporcionadoPorCliente":false,"notas":"instalacion incluida"}
JSON

echo

log "9) Validar listados"
curl -sS "$BASE_URL/clientes" -H "$AUTH"; echo
curl -sS "$BASE_URL/bicicletas" -H "$AUTH"; echo
curl -sS "$BASE_URL/ordenes" -H "$AUTH"; echo

log "Flujo OK"
