ALTER TABLE multimedia
    ADD COLUMN IF NOT EXISTS object_key VARCHAR(512);

CREATE UNIQUE INDEX IF NOT EXISTS uk_multimedia_object_key
    ON multimedia(object_key)
    WHERE object_key IS NOT NULL;
