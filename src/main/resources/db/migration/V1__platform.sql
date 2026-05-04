CREATE EXTENSION IF NOT EXISTS "pgcrypto";

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

INSERT INTO talleres (id, nombre, rut, plan_saas, activo, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'VeloService', '76.123.456-7', 'basico', TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_talleres_updated_at BEFORE UPDATE ON talleres FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_sucursales_updated_at BEFORE UPDATE ON sucursales FOR EACH ROW EXECUTE FUNCTION set_updated_at();
