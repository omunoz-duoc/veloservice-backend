CREATE TYPE estado_compra_enum AS ENUM ('borrador', 'confirmada', 'recibida', 'cancelada');

CREATE TABLE public.compras (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sucursal_proveedor_id UUID NOT NULL REFERENCES public.sucursal_proveedores(id),
    usuario_id UUID NOT NULL REFERENCES public.usuarios(id),
    numero_factura TEXT,
    neto NUMERIC NOT NULL DEFAULT 0,
    iva NUMERIC NOT NULL DEFAULT 0,
    total NUMERIC NOT NULL DEFAULT 0,
    estado estado_compra_enum NOT NULL DEFAULT 'borrador',
    fecha_compra DATE,
    fecha_recepcion DATE,
    notas TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
