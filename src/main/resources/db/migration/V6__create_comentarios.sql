CREATE TABLE comentarios (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id   UUID NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    texto      TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_comentarios_orden ON comentarios(orden_id);
