CREATE TABLE public.membresias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    descripcion TEXT,
    porcentaje_descuento NUMERIC NOT NULL DEFAULT 0
        CHECK (porcentaje_descuento >= 0 AND porcentaje_descuento <= 100),
    prioridad_atencion INTEGER NOT NULL DEFAULT 0,
    color_badge TEXT,
    activo BOOLEAN NOT NULL DEFAULT true
);
