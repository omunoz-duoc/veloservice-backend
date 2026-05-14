CREATE TABLE public.sucursal_proveedores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id UUID NOT NULL REFERENCES public.sucursales(id),
    proveedor_id UUID NOT NULL REFERENCES public.proveedores(id),
    codigo_cliente TEXT,
    condicion_pago TEXT,
    contacto_asignado TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
