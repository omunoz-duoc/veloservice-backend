CREATE TYPE tipo_doc_enum AS ENUM ('boleta', 'factura');
CREATE TYPE metodo_pago_enum AS ENUM ('efectivo', 'debito', 'credito', 'transferencia');
CREATE TYPE estado_cobro_enum AS ENUM ('pendiente', 'pagado', 'anulado');
CREATE TYPE estado_sii_enum AS ENUM ('no_aplica', 'pendiente', 'enviado', 'aceptado', 'rechazado');

CREATE TABLE public.cobros (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL UNIQUE REFERENCES public.ordenes(id),
    usuario_id UUID NOT NULL REFERENCES public.usuarios(id),
    tipo_documento tipo_doc_enum NOT NULL DEFAULT 'boleta',
    numero_documento TEXT,
    subtotal_servicios NUMERIC NOT NULL DEFAULT 0,
    subtotal_productos NUMERIC NOT NULL DEFAULT 0,
    descuento_membresia NUMERIC NOT NULL DEFAULT 0,
    descuento_manual NUMERIC NOT NULL DEFAULT 0,
    neto NUMERIC NOT NULL DEFAULT 0,
    iva NUMERIC NOT NULL DEFAULT 0,
    total NUMERIC NOT NULL DEFAULT 0,
    metodo_pago metodo_pago_enum NOT NULL DEFAULT 'efectivo',
    estado estado_cobro_enum NOT NULL DEFAULT 'pendiente',
    folio_sii TEXT,
    estado_sii estado_sii_enum NOT NULL DEFAULT 'no_aplica',
    fecha_pago TIMESTAMPTZ,
    anulada_at TIMESTAMPTZ,
    motivo_anulacion TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
