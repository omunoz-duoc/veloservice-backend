ALTER TABLE orden_productos
    ADD COLUMN IF NOT EXISTS usuario_id UUID REFERENCES usuarios(id);

ALTER TABLE orden_servicios
    ADD COLUMN IF NOT EXISTS usuario_id UUID REFERENCES usuarios(id);

ALTER TABLE multimedia
    ALTER COLUMN etapa DROP NOT NULL;

ALTER TABLE multimedia
    ALTER COLUMN tipo_archivo TYPE VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_orden_productos_usuario ON orden_productos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_orden_servicios_usuario ON orden_servicios(usuario_id);
