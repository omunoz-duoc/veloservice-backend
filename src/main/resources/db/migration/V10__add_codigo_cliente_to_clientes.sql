ALTER TABLE clientes ADD COLUMN IF NOT EXISTS codigo_cliente VARCHAR(20);

WITH maximos AS (
    SELECT
        taller_id,
        COALESCE(MAX((regexp_match(codigo_cliente, '^CL-(\d+)$'))[1]::INT), 0) AS max_numero
    FROM clientes
    WHERE codigo_cliente IS NOT NULL
    GROUP BY taller_id
),
pendientes AS (
    SELECT
        id,
        taller_id,
        ROW_NUMBER() OVER (PARTITION BY taller_id ORDER BY created_at, id) AS fila
    FROM clientes
    WHERE codigo_cliente IS NULL
),
numerados AS (
    SELECT
        pendientes.id,
        'CL-' || LPAD((COALESCE(maximos.max_numero, 0) + pendientes.fila)::TEXT, 4, '0') AS codigo_cliente
    FROM pendientes
    LEFT JOIN maximos ON maximos.taller_id = pendientes.taller_id
)
UPDATE clientes c
SET codigo_cliente = numerados.codigo_cliente
FROM numerados
WHERE c.id = numerados.id
  AND c.codigo_cliente IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_clientes_taller_codigo_cliente
    ON clientes(taller_id, codigo_cliente);

ALTER TABLE clientes ALTER COLUMN codigo_cliente SET NOT NULL;
