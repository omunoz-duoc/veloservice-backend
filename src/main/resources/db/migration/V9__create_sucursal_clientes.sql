CREATE TABLE public.sucursal_clientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id UUID NOT NULL REFERENCES public.sucursales(id),
    cliente_id UUID NOT NULL REFERENCES public.clientes(id),
    membresia_id UUID REFERENCES public.membresias(id),
    notas TEXT,
    membresia_desde TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
