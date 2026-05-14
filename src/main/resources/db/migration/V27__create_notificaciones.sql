CREATE TYPE canal_notif_enum AS ENUM ('email', 'sms', 'whatsapp');
CREATE TYPE tipo_notif_enum AS ENUM ('ingreso', 'listo', 'entrega', 'recordatorio');
CREATE TYPE estado_notif_enum AS ENUM ('pendiente', 'enviada', 'fallida');

CREATE TABLE public.notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orden_id UUID NOT NULL REFERENCES public.ordenes(id),
    canal canal_notif_enum NOT NULL,
    tipo tipo_notif_enum NOT NULL,
    mensaje TEXT NOT NULL,
    estado estado_notif_enum NOT NULL DEFAULT 'pendiente',
    intentos INTEGER NOT NULL DEFAULT 0,
    programada_para TIMESTAMPTZ,
    enviada_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
