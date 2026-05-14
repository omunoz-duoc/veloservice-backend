CREATE TABLE public.sucursal_servicios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id UUID NOT NULL REFERENCES public.sucursales(id),
    servicio_id UUID NOT NULL REFERENCES public.servicios(id),
    precio_personalizado NUMERIC,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
