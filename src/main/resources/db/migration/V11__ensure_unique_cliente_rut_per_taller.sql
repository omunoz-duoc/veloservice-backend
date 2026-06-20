CREATE UNIQUE INDEX IF NOT EXISTS idx_clientes_taller_rut
    ON clientes(taller_id, rut)
    WHERE rut IS NOT NULL;
