ALTER TABLE planes_saas
    ADD COLUMN max_sucursales  INT             NOT NULL DEFAULT 1,
    ADD COLUMN max_usuarios    INT             NOT NULL DEFAULT 5,
    ADD COLUMN max_ordenes_mes INT             NULL,
    ADD COLUMN precio_mensual  NUMERIC(10, 2)  NOT NULL DEFAULT 0,
    ADD COLUMN precio_anual    NUMERIC(10, 2)  NULL,
    ADD COLUMN trial_dias      INT             NOT NULL DEFAULT 0,
    ADD COLUMN features        JSONB           NOT NULL DEFAULT '{}';

COMMENT ON COLUMN planes_saas.max_sucursales  IS 'Máximo de sucursales permitidas para el plan.';
COMMENT ON COLUMN planes_saas.max_usuarios    IS 'Máximo de usuarios activos permitidos.';
COMMENT ON COLUMN planes_saas.max_ordenes_mes IS 'Máximo de órdenes de trabajo por mes. NULL = ilimitado.';
COMMENT ON COLUMN planes_saas.precio_mensual  IS 'Precio mensual en CLP.';
COMMENT ON COLUMN planes_saas.precio_anual    IS 'Precio anual en CLP con descuento. NULL si no aplica.';
COMMENT ON COLUMN planes_saas.trial_dias      IS 'Días de prueba gratuita al activar el plan.';
COMMENT ON COLUMN planes_saas.features        IS 'Feature flags del plan, ej: {"reportes_avanzados": true}.';
