CREATE TYPE estado_garantia_enum AS ENUM ('abierta', 'en_proceso', 'cerrada', 'rechazada');

CREATE TABLE public.garantias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES public.ordenes(id),
    numero_garantia TEXT NOT NULL UNIQUE,
    marca_bicicleta TEXT,
    componente_afectado TEXT,
    descripcion_falla TEXT,
    estado estado_garantia_enum NOT NULL DEFAULT 'abierta',
    fecha_inicio DATE NOT NULL DEFAULT CURRENT_DATE,
    fecha_vencimiento DATE,
    condiciones TEXT,
    resolucion TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
