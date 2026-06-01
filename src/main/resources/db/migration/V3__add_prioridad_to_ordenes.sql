ALTER TABLE ordenes
    ADD COLUMN prioridad VARCHAR(10) CHECK (prioridad IN ('alta', 'media', 'baja'));
