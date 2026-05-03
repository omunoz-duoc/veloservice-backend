-- ============================================================
--  MER Bikeshop - PostgreSQL Schema
--  Hierarchy: talleres → sucursales → (rest of entities)
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
--  ENUMS
-- ============================================================

CREATE TYPE plan_saas_enum       AS ENUM ('basico', 'profesional', 'enterprise');
CREATE TYPE estado_orden_enum    AS ENUM ('recibida', 'en_diagnostico', 'esperando_repuestos', 'en_reparacion', 'control_calidad', 'lista_para_entrega', 'entregada', 'cancelada');
CREATE TYPE tipo_orden_enum      AS ENUM ('reparacion', 'mantencion', 'garantia', 'venta');
CREATE TYPE estado_garantia_enum AS ENUM ('abierta', 'en_revision', 'aprobada', 'rechazada', 'resuelta');
CREATE TYPE tipo_movimiento_enum AS ENUM ('entrada', 'salida', 'ajuste', 'devolucion');
CREATE TYPE canal_notif_enum     AS ENUM ('email', 'whatsapp', 'sms', 'push');
CREATE TYPE tipo_notif_enum      AS ENUM ('confirmacion', 'actualizacion_estado', 'recordatorio', 'listo_para_entrega', 'factura');
CREATE TYPE estado_notif_enum    AS ENUM ('pendiente', 'enviada', 'fallida', 'cancelada');
CREATE TYPE tipo_doc_enum        AS ENUM ('boleta', 'factura', 'nota_credito');
CREATE TYPE metodo_pago_enum     AS ENUM ('efectivo', 'debito', 'credito', 'transferencia', 'otro');
CREATE TYPE estado_cobro_enum    AS ENUM ('pendiente', 'pagado', 'anulado');
CREATE TYPE estado_sii_enum      AS ENUM ('pendiente', 'aceptado', 'rechazado', 'no_aplica');
CREATE TYPE estado_compra_enum   AS ENUM ('borrador', 'confirmada', 'recibida_parcial', 'recibida', 'anulada');
CREATE TYPE tipo_archivo_enum    AS ENUM ('imagen', 'video', 'documento');
CREATE TYPE etapa_multimedia_enum AS ENUM ('ingreso', 'diagnostico', 'reparacion', 'entrega');


-- ============================================================
--  NIVEL 1 – TALLERES  (empresa / casa matriz)
-- ============================================================

CREATE TABLE talleres (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT        NOT NULL,
    rut         TEXT        NOT NULL UNIQUE,
    telefono    TEXT,
    email       TEXT,
    plan_saas   plan_saas_enum NOT NULL DEFAULT 'basico',
    logo_url    TEXT,
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


-- ============================================================
--  NIVEL 2 – SUCURSALES  (branch / location)
--  Every foreign key that used to point at talleres now
--  points here instead. talleres is the corporate umbrella.
-- ============================================================

CREATE TABLE sucursales (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    taller_id   UUID        NOT NULL REFERENCES talleres(id) ON DELETE RESTRICT,
    nombre      TEXT        NOT NULL,
    direccion   TEXT,
    telefono    TEXT,
    email       TEXT,
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sucursales_taller ON sucursales(taller_id);


-- ============================================================
--  ROLES, MÓDULOS Y PERMISOS  (sin dependencia de sucursal)
-- ============================================================

CREATE TABLE roles (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT    NOT NULL UNIQUE,
    descripcion TEXT,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE modulos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT NOT NULL UNIQUE,
    descripcion TEXT,
    ruta        TEXT NOT NULL
);

CREATE TABLE rol_permisos (
    id              UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    rol_id          UUID    NOT NULL REFERENCES roles(id)   ON DELETE CASCADE,
    modulo_id       UUID    NOT NULL REFERENCES modulos(id) ON DELETE CASCADE,
    puede_ver       BOOLEAN NOT NULL DEFAULT FALSE,
    puede_crear     BOOLEAN NOT NULL DEFAULT FALSE,
    puede_editar    BOOLEAN NOT NULL DEFAULT FALSE,
    puede_eliminar  BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (rol_id, modulo_id)
);


-- ============================================================
--  USUARIOS  (scoped to sucursal)
-- ============================================================

CREATE TABLE usuarios (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id   UUID        NOT NULL REFERENCES sucursales(id) ON DELETE RESTRICT,
    rol_id        UUID        NOT NULL REFERENCES roles(id)      ON DELETE RESTRICT,
    nombre        TEXT        NOT NULL,
    apellido      TEXT        NOT NULL,
    rut           TEXT,
    email         TEXT        NOT NULL UNIQUE,
    telefono      TEXT,
    password_hash TEXT        NOT NULL,
    activo        BOOLEAN     NOT NULL DEFAULT TRUE,
    last_login    TIMESTAMPTZ,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_usuarios_sucursal ON usuarios(sucursal_id);
CREATE INDEX idx_usuarios_rol      ON usuarios(rol_id);


-- ============================================================
--  CLIENTES Y MEMBRESÍAS
-- ============================================================

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

CREATE TABLE membresias (
    id                       UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre                   TEXT    NOT NULL,
    descripcion              TEXT,
    porcentaje_descuento     NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (porcentaje_descuento BETWEEN 0 AND 100),
    prioridad_atencion       INT     NOT NULL DEFAULT 0,
    color_badge              TEXT,
    activo                   BOOLEAN NOT NULL DEFAULT TRUE
);

-- Relación cliente ↔ sucursal (antes taller_clientes → taller_id)
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


-- ============================================================
--  BICICLETAS
-- ============================================================

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


-- ============================================================
--  SERVICIOS
-- ============================================================

CREATE TABLE servicios (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT        NOT NULL,
    descripcion TEXT,
    precio_base NUMERIC(12,2) NOT NULL DEFAULT 0,
    es_garantia BOOLEAN     NOT NULL DEFAULT FALSE,
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Precio personalizado por sucursal (antes taller_servicios → taller_id)
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


-- ============================================================
--  PRODUCTOS E INVENTARIO
-- ============================================================

CREATE TABLE categorias_producto (
    id          UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre      TEXT    NOT NULL UNIQUE,
    descripcion TEXT,
    activo      BOOLEAN NOT NULL DEFAULT TRUE
);

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


-- ============================================================
--  PROVEEDORES
-- ============================================================

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

-- Relación proveedor ↔ sucursal (antes taller_proveedores → taller_id)
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


-- ============================================================
--  COMPRAS
-- ============================================================

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


-- ============================================================
--  ÓRDENES DE TRABAJO
-- ============================================================

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


-- ============================================================
--  MOVIMIENTOS DE STOCK
-- ============================================================

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


-- ============================================================
--  GARANTÍAS
-- ============================================================

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


-- ============================================================
--  MULTIMEDIA
-- ============================================================

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


-- ============================================================
--  NOTIFICACIONES
-- ============================================================

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


-- ============================================================
--  COBROS
-- ============================================================

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


-- ============================================================
--  TRIGGERS – updated_at automático
-- ============================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_talleres_updated_at
    BEFORE UPDATE ON talleres
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_sucursales_updated_at
    BEFORE UPDATE ON sucursales
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_clientes_updated_at
    BEFORE UPDATE ON clientes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_productos_updated_at
    BEFORE UPDATE ON productos
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_ordenes_updated_at
    BEFORE UPDATE ON ordenes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_garantias_updated_at
    BEFORE UPDATE ON garantias
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- ============================================================
--  COMENTARIOS
-- ============================================================

COMMENT ON TABLE talleres            IS 'Empresa / casa matriz propietaria de una o más sucursales.';
COMMENT ON TABLE sucursales          IS 'Ubicación física (branch) de un taller. Todas las operaciones quedan bajo una sucursal.';
COMMENT ON TABLE sucursal_clientes   IS 'Relación cliente ↔ sucursal con membresía opcional (renombrado desde taller_clientes).';
COMMENT ON TABLE sucursal_servicios  IS 'Precio personalizado de un servicio por sucursal (renombrado desde taller_servicios).';
COMMENT ON TABLE sucursal_proveedores IS 'Relación proveedor ↔ sucursal (renombrado desde taller_proveedores).';
