CREATE TABLE public.rol_permisos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rol_id UUID NOT NULL REFERENCES public.roles(id),
    modulo_id UUID NOT NULL REFERENCES public.modulos(id),
    puede_ver BOOLEAN NOT NULL DEFAULT false,
    puede_crear BOOLEAN NOT NULL DEFAULT false,
    puede_editar BOOLEAN NOT NULL DEFAULT false,
    puede_eliminar BOOLEAN NOT NULL DEFAULT false
);
