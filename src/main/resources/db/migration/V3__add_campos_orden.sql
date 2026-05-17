ALTER TABLE ordenes
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(50) NOT NULL DEFAULT 'mantencion',
    ADD COLUMN IF NOT EXISTS prioridad VARCHAR(50) NOT NULL DEFAULT 'MEDIA',
    ADD COLUMN IF NOT EXISTS fecha_estimada_entrega DATE,
    ADD COLUMN IF NOT EXISTS mecanico_asignado_id UUID REFERENCES usuarios(id),
    ADD COLUMN IF NOT EXISTS descripcion_trabajo TEXT,
    ADD COLUMN IF NOT EXISTS notas_internas TEXT;
