CREATE TABLE ordenes (
    id                              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id                     UUID        NOT NULL REFERENCES sucursales(id) ON DELETE RESTRICT,
    bicicleta_id                    UUID        NOT NULL REFERENCES bicicletas(id) ON DELETE RESTRICT,
    mecanico_id                     UUID        REFERENCES usuarios(id)            ON DELETE SET NULL,
    numero_orden                    TEXT        NOT NULL UNIQUE,
    estado                          estado_orden_enum NOT NULL DEFAULT 'recibida',
    tipo                            tipo_orden_enum   NOT NULL DEFAULT 'reparacion',
    diagnostico_inicial             TEXT,
    diagnostico_final               TEXT,
    observaciones_cliente           TEXT,
    descuento_manual                NUMERIC(12,2) NOT NULL DEFAULT 0,
    porcentaje_descuento_membresia  NUMERIC(5,2)  NOT NULL DEFAULT 0,
    fecha_ingreso                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    fecha_prometida                 TIMESTAMPTZ,
    fecha_entrega                   TIMESTAMPTZ,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_ordenes_sucursal   ON ordenes(sucursal_id);
CREATE INDEX idx_ordenes_bicicleta  ON ordenes(bicicleta_id);
CREATE INDEX idx_ordenes_mecanico   ON ordenes(mecanico_id);
CREATE INDEX idx_ordenes_estado     ON ordenes(estado);

CREATE TABLE orden_estados (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id        UUID        NOT NULL REFERENCES ordenes(id)   ON DELETE CASCADE,
    usuario_id      UUID        NOT NULL REFERENCES usuarios(id)  ON DELETE RESTRICT,
    estado_anterior estado_orden_enum,
    estado_nuevo    estado_orden_enum NOT NULL,
    observacion     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_orden_estados_orden ON orden_estados(orden_id);

CREATE TABLE orden_servicios (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id              UUID        NOT NULL REFERENCES ordenes(id)   ON DELETE CASCADE,
    servicio_id           UUID        NOT NULL REFERENCES servicios(id) ON DELETE RESTRICT,
    precio_base_snapshot  NUMERIC(12,2) NOT NULL DEFAULT 0,
    precio_aplicado       NUMERIC(12,2) NOT NULL DEFAULT 0,
    descuento_aplicado    NUMERIC(12,2) NOT NULL DEFAULT 0,
    notas                 TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_orden_servicios_orden ON orden_servicios(orden_id);

CREATE TABLE orden_productos (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id                UUID        NOT NULL REFERENCES ordenes(id)   ON DELETE CASCADE,
    producto_id             UUID        NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    cantidad                INT         NOT NULL CHECK (cantidad > 0),
    precio_costo_snapshot   NUMERIC(12,2) NOT NULL DEFAULT 0,
    precio_venta_snapshot   NUMERIC(12,2) NOT NULL DEFAULT 0,
    precio_aplicado         NUMERIC(12,2) NOT NULL DEFAULT 0,
    proporcionado_por_cliente BOOLEAN   NOT NULL DEFAULT FALSE,
    notas                   TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_orden_productos_orden   ON orden_productos(orden_id);
CREATE INDEX idx_orden_productos_producto ON orden_productos(producto_id);

CREATE TABLE multimedia (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id     UUID        NOT NULL REFERENCES ordenes(id)   ON DELETE CASCADE,
    usuario_id   UUID        NOT NULL REFERENCES usuarios(id)  ON DELETE RESTRICT,
    url          TEXT        NOT NULL,
    tipo_archivo tipo_archivo_enum    NOT NULL,
    etapa        etapa_multimedia_enum NOT NULL,
    descripcion  TEXT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_multimedia_orden ON multimedia(orden_id);

CREATE TABLE garantias (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id            UUID        NOT NULL REFERENCES ordenes(id) ON DELETE RESTRICT,
    numero_garantia     TEXT        NOT NULL UNIQUE,
    marca_bicicleta     TEXT,
    componente_afectado TEXT,
    descripcion_falla   TEXT,
    estado              estado_garantia_enum NOT NULL DEFAULT 'abierta',
    fecha_inicio        DATE        NOT NULL DEFAULT CURRENT_DATE,
    fecha_vencimiento   DATE,
    condiciones         TEXT,
    resolucion          TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_garantias_orden ON garantias(orden_id);

CREATE TRIGGER trg_ordenes_updated_at BEFORE UPDATE ON ordenes FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_garantias_updated_at BEFORE UPDATE ON garantias FOR EACH ROW EXECUTE FUNCTION set_updated_at();
