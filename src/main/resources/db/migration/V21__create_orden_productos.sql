CREATE TABLE public.orden_productos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES public.ordenes(id),
    producto_id UUID NOT NULL REFERENCES public.productos(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_costo_snapshot NUMERIC NOT NULL DEFAULT 0,
    precio_venta_snapshot NUMERIC NOT NULL DEFAULT 0,
    precio_aplicado NUMERIC NOT NULL DEFAULT 0,
    proporcionado_por_cliente BOOLEAN NOT NULL DEFAULT false,
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
