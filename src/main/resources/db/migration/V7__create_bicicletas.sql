CREATE TABLE public.bicicletas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id UUID NOT NULL REFERENCES public.clientes(id),
    marca TEXT,
    modelo TEXT,
    tipo TEXT,
    aro TEXT,
    color TEXT,
    numero_serie TEXT,
    anio INTEGER,
    foto_url TEXT,
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
