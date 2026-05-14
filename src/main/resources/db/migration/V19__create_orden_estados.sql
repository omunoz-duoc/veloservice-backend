CREATE TABLE public.orden_estados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES public.ordenes(id),
    usuario_id UUID NOT NULL REFERENCES public.usuarios(id),
    estado_anterior estado_orden_enum,
    estado_nuevo estado_orden_enum NOT NULL,
    observacion TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
