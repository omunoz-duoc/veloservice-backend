-- multimedia.tipo_archivo now stores the file MIME type (e.g. image/png,
-- video/mp4, application/pdf), not the legacy category enum. The column was
-- widened to VARCHAR(100) in V5 and the read query derives the category from
-- the MIME prefix, but the original baseline CHECK
-- (tipo_archivo IN ('imagen','video','documento')) was never dropped, so any
-- MIME value fails it. Drop the stale CHECK.
ALTER TABLE multimedia DROP CONSTRAINT IF EXISTS multimedia_tipo_archivo_check;

-- etapa gained 'recepcion' and 'calidad' (EtapaMultimediaEnum) and became
-- nullable in V5. Refresh the CHECK to match the enum and allow NULL.
ALTER TABLE multimedia DROP CONSTRAINT IF EXISTS multimedia_etapa_check;
ALTER TABLE multimedia ADD CONSTRAINT multimedia_etapa_check
    CHECK (etapa IS NULL OR etapa IN
        ('ingreso', 'recepcion', 'diagnostico', 'reparacion', 'calidad', 'entrega'));
