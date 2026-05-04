CREATE TABLE proveedores (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT        NOT NULL,
    rut         TEXT,
    telefono    TEXT,
    email       TEXT,
    direccion   TEXT,
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sucursal_proveedores (
    id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id       UUID        NOT NULL REFERENCES sucursales(id)   ON DELETE RESTRICT,
    proveedor_id      UUID        NOT NULL REFERENCES proveedores(id)  ON DELETE RESTRICT,
    codigo_cliente    TEXT,
    condicion_pago    TEXT,
    contacto_asignado TEXT,
    activo            BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (sucursal_id, proveedor_id)
);
CREATE INDEX idx_sucursal_proveedores_sucursal  ON sucursal_proveedores(sucursal_id);
CREATE INDEX idx_sucursal_proveedores_proveedor ON sucursal_proveedores(proveedor_id);

CREATE TABLE productos (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id   UUID        NOT NULL REFERENCES sucursales(id)          ON DELETE RESTRICT,
    categoria_id  UUID        REFERENCES categorias_producto(id)          ON DELETE SET NULL,
    nombre        TEXT        NOT NULL,
    sku           TEXT,
    marca         TEXT,
    unidad_medida TEXT        NOT NULL DEFAULT 'unidad',
    precio_costo  NUMERIC(12,2) NOT NULL DEFAULT 0,
    precio_venta  NUMERIC(12,2) NOT NULL DEFAULT 0,
    stock         INT         NOT NULL DEFAULT 0,
    stock_minimo  INT         NOT NULL DEFAULT 0,
    activo        BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_productos_sucursal  ON productos(sucursal_id);
CREATE INDEX idx_productos_categoria ON productos(categoria_id);
CREATE INDEX idx_productos_sku       ON productos(sku);

CREATE TABLE compras (
    id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_proveedor_id UUID        NOT NULL REFERENCES sucursal_proveedores(id) ON DELETE RESTRICT,
    usuario_id            UUID        NOT NULL REFERENCES usuarios(id)             ON DELETE RESTRICT,
    numero_factura        TEXT,
    neto                  NUMERIC(12,2) NOT NULL DEFAULT 0,
    iva                   NUMERIC(12,2) NOT NULL DEFAULT 0,
    total                 NUMERIC(12,2) NOT NULL DEFAULT 0,
    estado                estado_compra_enum NOT NULL DEFAULT 'borrador',
    fecha_compra          DATE,
    fecha_recepcion       DATE,
    notas                 TEXT,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_compras_sucursal_proveedor ON compras(sucursal_proveedor_id);

CREATE TABLE compra_productos (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    compra_id       UUID        NOT NULL REFERENCES compras(id)   ON DELETE CASCADE,
    producto_id     UUID        NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    cantidad        INT         NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12,2) NOT NULL DEFAULT 0,
    subtotal        NUMERIC(12,2) NOT NULL DEFAULT 0
);
CREATE INDEX idx_compra_productos_compra   ON compra_productos(compra_id);
CREATE INDEX idx_compra_productos_producto ON compra_productos(producto_id);

CREATE TABLE movimientos_stock (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    producto_id     UUID        NOT NULL REFERENCES productos(id)  ON DELETE RESTRICT,
    orden_id        UUID        REFERENCES ordenes(id)             ON DELETE SET NULL,
    compra_id       UUID        REFERENCES compras(id)             ON DELETE SET NULL,
    usuario_id      UUID        NOT NULL REFERENCES usuarios(id)   ON DELETE RESTRICT,
    tipo            tipo_movimiento_enum NOT NULL,
    cantidad        INT         NOT NULL,
    stock_anterior  INT         NOT NULL,
    stock_posterior INT         NOT NULL,
    motivo          TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_mov_stock_producto ON movimientos_stock(producto_id);
CREATE INDEX idx_mov_stock_orden    ON movimientos_stock(orden_id);
CREATE INDEX idx_mov_stock_compra   ON movimientos_stock(compra_id);

CREATE TRIGGER trg_productos_updated_at BEFORE UPDATE ON productos FOR EACH ROW EXECUTE FUNCTION set_updated_at();
