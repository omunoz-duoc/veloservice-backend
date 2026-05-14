CREATE TYPE tipo_movimiento_enum AS ENUM ('entrada', 'salida', 'ajuste');

CREATE TABLE public.movimientos_stock (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    producto_id UUID NOT NULL REFERENCES public.productos(id),
    orden_id UUID REFERENCES public.ordenes(id),
    compra_id UUID REFERENCES public.compras(id),
    usuario_id UUID NOT NULL REFERENCES public.usuarios(id),
    tipo tipo_movimiento_enum NOT NULL,
    cantidad INTEGER NOT NULL,
    stock_anterior INTEGER NOT NULL,
    stock_posterior INTEGER NOT NULL,
    motivo TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
