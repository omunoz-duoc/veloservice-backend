CREATE TABLE clientes (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT        NOT NULL,
    apellido    TEXT        NOT NULL,
    rut         TEXT,
    telefono    TEXT,
    email       TEXT,
    direccion   TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE bicicletas (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id    UUID        NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
    marca         TEXT,
    modelo        TEXT,
    tipo          TEXT,
    aro           TEXT,
    color         TEXT,
    numero_serie  TEXT,
    anio          INT,
    foto_url      TEXT,
    notas         TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_bicicletas_cliente ON bicicletas(cliente_id);

CREATE TABLE sucursal_clientes (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id      UUID        NOT NULL REFERENCES sucursales(id) ON DELETE RESTRICT,
    cliente_id       UUID        NOT NULL REFERENCES clientes(id)   ON DELETE RESTRICT,
    membresia_id     UUID        REFERENCES membresias(id)          ON DELETE SET NULL,
    notas            TEXT,
    membresia_desde  TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (sucursal_id, cliente_id)
);
CREATE INDEX idx_sucursal_clientes_sucursal  ON sucursal_clientes(sucursal_id);
CREATE INDEX idx_sucursal_clientes_cliente   ON sucursal_clientes(cliente_id);

CREATE TRIGGER trg_clientes_updated_at BEFORE UPDATE ON clientes FOR EACH ROW EXECUTE FUNCTION set_updated_at();
