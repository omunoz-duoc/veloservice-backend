CREATE TABLE categorias_producto (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT    NOT NULL UNIQUE,
    descripcion TEXT,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE servicios (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT        NOT NULL,
    descripcion TEXT,
    precio_base NUMERIC(12,2) NOT NULL DEFAULT 0,
    es_garantia BOOLEAN     NOT NULL DEFAULT FALSE,
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE membresias (
    id                       UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre                   TEXT    NOT NULL,
    descripcion              TEXT,
    porcentaje_descuento     NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (porcentaje_descuento BETWEEN 0 AND 100),
    prioridad_atencion       INT     NOT NULL DEFAULT 0,
    color_badge              TEXT,
    activo                   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE sucursal_servicios (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id          UUID        NOT NULL REFERENCES sucursales(id) ON DELETE RESTRICT,
    servicio_id          UUID        NOT NULL REFERENCES servicios(id)  ON DELETE RESTRICT,
    precio_personalizado NUMERIC(12,2),
    activo               BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (sucursal_id, servicio_id)
);
CREATE INDEX idx_sucursal_servicios_sucursal ON sucursal_servicios(sucursal_id);
