CREATE TYPE tipo_archivo_enum AS ENUM ('imagen', 'video', 'documento');
CREATE TYPE etapa_multimedia_enum AS ENUM ('ingreso', 'proceso', 'entrega');

CREATE TABLE public.multimedia (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES public.ordenes(id),
    usuario_id UUID NOT NULL REFERENCES public.usuarios(id),
    url TEXT NOT NULL,
    tipo_archivo tipo_archivo_enum NOT NULL,
    etapa etapa_multimedia_enum NOT NULL,
    descripcion TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
