#!/usr/bin/env bash
set -euo pipefail

psql "postgresql://velo_user:velo_pass@localhost:5433/veloservice_db" -v ON_ERROR_STOP=1 <<'SQL'
BEGIN;

CREATE TEMP TABLE tmp_demo_clientes AS
  SELECT id FROM clientes
  WHERE email = 'ana@dominio.cl' OR rut = '12345678-9';

CREATE TEMP TABLE tmp_demo_bicis AS
  SELECT id FROM bicicletas WHERE cliente_id IN (SELECT id FROM tmp_demo_clientes);

CREATE TEMP TABLE tmp_demo_ordenes AS
  SELECT id FROM ordenes WHERE bicicleta_id IN (SELECT id FROM tmp_demo_bicis);

CREATE TEMP TABLE tmp_demo_productos AS
  SELECT id FROM productos
  WHERE nombre = 'Cadena 11v' AND sku LIKE 'CAD-11V-%';

CREATE TEMP TABLE tmp_demo_servicios AS
  SELECT id FROM servicios
  WHERE nombre = 'Ajuste frenos' AND descripcion = 'Ajuste completo';

DELETE FROM orden_productos
WHERE orden_id IN (SELECT id FROM tmp_demo_ordenes)
   OR producto_id IN (SELECT id FROM tmp_demo_productos);

DELETE FROM orden_servicios
WHERE orden_id IN (SELECT id FROM tmp_demo_ordenes)
   OR servicio_id IN (SELECT id FROM tmp_demo_servicios);

DELETE FROM orden_estados
WHERE orden_id IN (SELECT id FROM tmp_demo_ordenes);

DELETE FROM multimedia
WHERE orden_id IN (SELECT id FROM tmp_demo_ordenes);

DELETE FROM movimientos_stock
WHERE orden_id IN (SELECT id FROM tmp_demo_ordenes)
   OR producto_id IN (SELECT id FROM tmp_demo_productos);

DELETE FROM ordenes
WHERE id IN (SELECT id FROM tmp_demo_ordenes);

DELETE FROM bicicletas
WHERE id IN (SELECT id FROM tmp_demo_bicis);

DELETE FROM sucursal_clientes
WHERE cliente_id IN (SELECT id FROM tmp_demo_clientes);

DELETE FROM clientes
WHERE id IN (SELECT id FROM tmp_demo_clientes);

DELETE FROM sucursal_servicios
WHERE servicio_id IN (SELECT id FROM tmp_demo_servicios);

DELETE FROM servicios
WHERE id IN (SELECT id FROM tmp_demo_servicios);

DELETE FROM productos
WHERE id IN (SELECT id FROM tmp_demo_productos);

COMMIT;
SQL

echo "Limpieza OK."
