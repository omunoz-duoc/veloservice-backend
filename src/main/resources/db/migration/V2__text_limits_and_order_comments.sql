ALTER TABLE usuarios_plataforma
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN apellido TYPE VARCHAR(120),
    ALTER COLUMN email TYPE VARCHAR(254),
    ALTER COLUMN telefono TYPE VARCHAR(30),
    ALTER COLUMN password_hash TYPE VARCHAR(255);

ALTER TABLE planes_saas
    ALTER COLUMN codigo TYPE VARCHAR(50),
    ALTER COLUMN nombre TYPE VARCHAR(120);

ALTER TABLE talleres
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN rut TYPE VARCHAR(20),
    ALTER COLUMN telefono TYPE VARCHAR(30),
    ALTER COLUMN email TYPE VARCHAR(254),
    ALTER COLUMN logo_url TYPE VARCHAR(2048);

ALTER TABLE sucursales
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN direccion TYPE VARCHAR(255),
    ALTER COLUMN telefono TYPE VARCHAR(30),
    ALTER COLUMN email TYPE VARCHAR(254);

ALTER TABLE roles
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN ambito TYPE VARCHAR(20);

ALTER TABLE modulos
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN ruta TYPE VARCHAR(255);

ALTER TABLE usuarios
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN apellido TYPE VARCHAR(120),
    ALTER COLUMN rut TYPE VARCHAR(20),
    ALTER COLUMN email TYPE VARCHAR(254),
    ALTER COLUMN telefono TYPE VARCHAR(30),
    ALTER COLUMN password_hash TYPE VARCHAR(255);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token_hash ON password_reset_tokens(token_hash);

ALTER TABLE membresias
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN color_badge TYPE VARCHAR(30);

ALTER TABLE clientes
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN apellido TYPE VARCHAR(120),
    ALTER COLUMN rut TYPE VARCHAR(20),
    ALTER COLUMN telefono TYPE VARCHAR(30),
    ALTER COLUMN email TYPE VARCHAR(254),
    ALTER COLUMN direccion TYPE VARCHAR(255);

ALTER TABLE bicicletas
    ALTER COLUMN marca TYPE VARCHAR(80),
    ALTER COLUMN modelo TYPE VARCHAR(80),
    ALTER COLUMN tipo TYPE VARCHAR(80),
    ALTER COLUMN aro TYPE VARCHAR(80),
    ALTER COLUMN color TYPE VARCHAR(80),
    ALTER COLUMN numero_serie TYPE VARCHAR(120),
    ALTER COLUMN foto_url TYPE VARCHAR(2048);

ALTER TABLE servicios
    ALTER COLUMN nombre TYPE VARCHAR(120);

ALTER TABLE categorias_producto
    ALTER COLUMN nombre TYPE VARCHAR(120);

ALTER TABLE productos
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN sku TYPE VARCHAR(80),
    ALTER COLUMN marca TYPE VARCHAR(80),
    ALTER COLUMN unidad_medida TYPE VARCHAR(30);

ALTER TABLE proveedores
    ALTER COLUMN nombre TYPE VARCHAR(120),
    ALTER COLUMN rut TYPE VARCHAR(20),
    ALTER COLUMN telefono TYPE VARCHAR(30),
    ALTER COLUMN email TYPE VARCHAR(254),
    ALTER COLUMN direccion TYPE VARCHAR(255);

ALTER TABLE sucursal_proveedores
    ALTER COLUMN codigo_cliente TYPE VARCHAR(80),
    ALTER COLUMN condicion_pago TYPE VARCHAR(120),
    ALTER COLUMN contacto_asignado TYPE VARCHAR(120);

ALTER TABLE estados_compra
    ALTER COLUMN codigo TYPE VARCHAR(50),
    ALTER COLUMN nombre TYPE VARCHAR(120);

ALTER TABLE compras
    ALTER COLUMN numero_factura TYPE VARCHAR(50);

ALTER TABLE estados_orden
    ALTER COLUMN codigo TYPE VARCHAR(50),
    ALTER COLUMN nombre TYPE VARCHAR(120);

ALTER TABLE tipos_orden
    ALTER COLUMN codigo TYPE VARCHAR(50),
    ALTER COLUMN nombre TYPE VARCHAR(120);

ALTER TABLE ordenes
    ALTER COLUMN numero_orden TYPE VARCHAR(30);

CREATE TABLE IF NOT EXISTS orden_comentarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES ordenes(id) ON DELETE CASCADE,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    texto TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orden_comentarios_orden ON orden_comentarios(orden_id);
CREATE INDEX IF NOT EXISTS idx_orden_comentarios_usuario ON orden_comentarios(usuario_id);

DO $$
BEGIN
    IF to_regclass('public.comentarios') IS NOT NULL THEN
        INSERT INTO orden_comentarios (id, orden_id, usuario_id, texto, created_at)
        SELECT id, orden_id, usuario_id, texto, created_at
        FROM comentarios
        ON CONFLICT (id) DO NOTHING;
    END IF;
END;
$$;

ALTER TABLE traslados
    ALTER COLUMN estado TYPE VARCHAR(30);

ALTER TABLE movimientos_stock
    ALTER COLUMN tipo TYPE VARCHAR(30);

ALTER TABLE estados_garantia
    ALTER COLUMN codigo TYPE VARCHAR(50),
    ALTER COLUMN nombre TYPE VARCHAR(120);

ALTER TABLE garantias
    ALTER COLUMN numero_garantia TYPE VARCHAR(30),
    ALTER COLUMN marca_bicicleta TYPE VARCHAR(120),
    ALTER COLUMN componente_afectado TYPE VARCHAR(120);

ALTER TABLE multimedia
    ALTER COLUMN url TYPE VARCHAR(2048),
    ALTER COLUMN tipo_archivo TYPE VARCHAR(20),
    ALTER COLUMN etapa TYPE VARCHAR(30);

ALTER TABLE notificaciones
    ALTER COLUMN canal TYPE VARCHAR(20),
    ALTER COLUMN tipo TYPE VARCHAR(30),
    ALTER COLUMN estado TYPE VARCHAR(30);

ALTER TABLE cobros
    ALTER COLUMN tipo_documento TYPE VARCHAR(30),
    ALTER COLUMN numero_documento TYPE VARCHAR(50),
    ALTER COLUMN metodo_pago TYPE VARCHAR(30),
    ALTER COLUMN estado TYPE VARCHAR(30),
    ALTER COLUMN folio_sii TYPE VARCHAR(50),
    ALTER COLUMN estado_sii TYPE VARCHAR(30);

COMMENT ON TABLE password_reset_tokens IS 'Tokens hasheados para recuperación de contraseña de usuarios operativos.';
COMMENT ON TABLE orden_comentarios IS 'Comentarios libres asociados a una orden de trabajo.';
