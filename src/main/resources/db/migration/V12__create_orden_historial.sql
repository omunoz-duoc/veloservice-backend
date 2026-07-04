-- Audit trail unificado de modificaciones a una orden de trabajo.
-- Estrategia: poblado desde el service layer (no triggers) para capturar usuario e intención.
CREATE TABLE IF NOT EXISTS orden_historial (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id    UUID         NOT NULL REFERENCES ordenes(id)  ON DELETE CASCADE,
    usuario_id  UUID         REFERENCES usuarios(id)          ON DELETE SET NULL,
    accion      VARCHAR(40)  NOT NULL,
    entidad     VARCHAR(40),
    entidad_id  UUID,
    detalle     JSONB,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orden_historial_orden
    ON orden_historial(orden_id, created_at);
