CREATE TYPE plan_saas_enum AS ENUM ('basico', 'profesional', 'enterprise');

CREATE TABLE public.talleres (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre TEXT NOT NULL,
    rut TEXT NOT NULL UNIQUE,
    telefono TEXT,
    email TEXT,
    plan_saas plan_saas_enum NOT NULL DEFAULT 'basico',
    logo_url TEXT,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
