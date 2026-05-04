CREATE TABLE cobros (
    id                       UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id                 UUID        NOT NULL UNIQUE REFERENCES ordenes(id) ON DELETE RESTRICT,
    usuario_id               UUID        NOT NULL REFERENCES usuarios(id)       ON DELETE RESTRICT,
    tipo_documento           tipo_doc_enum    NOT NULL DEFAULT 'boleta',
    numero_documento         TEXT,
    subtotal_servicios       NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal_productos       NUMERIC(12,2) NOT NULL DEFAULT 0,
    descuento_membresia      NUMERIC(12,2) NOT NULL DEFAULT 0,
    descuento_manual         NUMERIC(12,2) NOT NULL DEFAULT 0,
    neto                     NUMERIC(12,2) NOT NULL DEFAULT 0,
    iva                      NUMERIC(12,2) NOT NULL DEFAULT 0,
    total                    NUMERIC(12,2) NOT NULL DEFAULT 0,
    metodo_pago              metodo_pago_enum  NOT NULL DEFAULT 'efectivo',
    estado                   estado_cobro_enum NOT NULL DEFAULT 'pendiente',
    folio_sii                TEXT,
    estado_sii               estado_sii_enum   NOT NULL DEFAULT 'no_aplica',
    fecha_pago               TIMESTAMPTZ,
    anulada_at               TIMESTAMPTZ,
    motivo_anulacion         TEXT,
    created_at               TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_cobros_orden ON cobros(orden_id);

CREATE TABLE notificaciones (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id         UUID        NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    canal            canal_notif_enum  NOT NULL,
    tipo             tipo_notif_enum   NOT NULL,
    mensaje          TEXT        NOT NULL,
    estado           estado_notif_enum NOT NULL DEFAULT 'pendiente',
    intentos         INT         NOT NULL DEFAULT 0,
    programada_para  TIMESTAMPTZ,
    enviada_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notificaciones_orden  ON notificaciones(orden_id);
CREATE INDEX idx_notificaciones_estado ON notificaciones(estado);
