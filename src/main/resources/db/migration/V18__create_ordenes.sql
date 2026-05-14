CREATE TYPE estado_orden_enum AS ENUM ('recibida', 'en_proceso', 'lista', 'entregada', 'cancelada');
CREATE TYPE tipo_orden_enum AS ENUM ('reparacion', 'mantencion', 'garantia');

CREATE TABLE public.ordenes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id UUID NOT NULL REFERENCES public.sucursales(id),
    bicicleta_id UUID NOT NULL REFERENCES public.bicicletas(id),
    mecanico_id UUID REFERENCES public.usuarios(id),
    numero_orden TEXT NOT NULL UNIQUE,
    estado estado_orden_enum NOT NULL DEFAULT 'recibida',
    tipo tipo_orden_enum NOT NULL DEFAULT 'reparacion',
    diagnostico_inicial TEXT,
    diagnostico_final TEXT,
    observaciones_cliente TEXT,
    descuento_manual NUMERIC NOT NULL DEFAULT 0,
    porcentaje_descuento_membresia NUMERIC NOT NULL DEFAULT 0,
    fecha_ingreso TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_prometida TIMESTAMPTZ,
    fecha_entrega TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    external_id VARCHAR UNIQUE
);
