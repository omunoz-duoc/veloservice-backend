CREATE TABLE public.productos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_id UUID NOT NULL REFERENCES public.sucursales(id),
    categoria_id UUID REFERENCES public.categorias_producto(id),
    nombre TEXT NOT NULL,
    sku TEXT,
    marca TEXT,
    unidad_medida TEXT NOT NULL DEFAULT 'unidad',
    precio_costo NUMERIC NOT NULL DEFAULT 0,
    precio_venta NUMERIC NOT NULL DEFAULT 0,
    stock INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
