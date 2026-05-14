CREATE TABLE public.orden_servicios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES public.ordenes(id),
    servicio_id UUID NOT NULL REFERENCES public.servicios(id),
    precio_base_snapshot NUMERIC NOT NULL DEFAULT 0,
    precio_aplicado NUMERIC NOT NULL DEFAULT 0,
    descuento_aplicado NUMERIC NOT NULL DEFAULT 0,
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
