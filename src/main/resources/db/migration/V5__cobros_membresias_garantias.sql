CREATE TABLE membresias (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    porcentaje_descuento DECIMAL(5,2) NOT NULL CHECK (porcentaje_descuento >= 0 AND porcentaje_descuento <= 100),
    prioridad_atencion INT,
    color_badge VARCHAR(20),
    activo BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE servicios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    precio_base DECIMAL(10,2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE orden_servicios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    orden_id UUID NOT NULL REFERENCES ordenes(id),
    servicio_id UUID NOT NULL REFERENCES servicios(id),
    precio_base_snapshot DECIMAL(10,2) NOT NULL,
    precio_aplicado DECIMAL(10,2) NOT NULL,
    descuento_aplicado DECIMAL(10,2) NOT NULL DEFAULT 0,
    notas TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE orden_productos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    orden_id UUID NOT NULL REFERENCES ordenes(id),
    producto_id UUID NOT NULL REFERENCES productos(id),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_costo_snapshot DECIMAL(10,2) NOT NULL,
    precio_venta_snapshot DECIMAL(10,2) NOT NULL,
    precio_aplicado DECIMAL(10,2) NOT NULL,
    proporcionado_por_cliente BOOLEAN NOT NULL DEFAULT false,
    notas TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE cobros (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    orden_id UUID NOT NULL REFERENCES ordenes(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    tipo_documento VARCHAR(20) NOT NULL CHECK (tipo_documento IN ('Boleta','Factura')),
    numero_documento VARCHAR(50) NOT NULL,
    subtotal_servicios DECIMAL(10,2) NOT NULL,
    subtotal_productos DECIMAL(10,2) NOT NULL,
    descuento_membresia DECIMAL(10,2) NOT NULL DEFAULT 0,
    descuento_manual DECIMAL(10,2) NOT NULL DEFAULT 0,
    neto DECIMAL(10,2) NOT NULL,
    iva DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(20) NOT NULL CHECK (metodo_pago IN ('Efectivo','Tarjeta','Transferencia')),
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('Pendiente','Pagado','Anulado')),
    fecha_pago TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE garantias (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    orden_producto_id UUID NOT NULL REFERENCES orden_productos(id),
    orden_id UUID NOT NULL REFERENCES ordenes(id),
    numero_garantia VARCHAR(50) NOT NULL UNIQUE,
    descripcion_falla TEXT NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('Activa','En_Evaluacion','Resuelta','Rechazada','Vencida')),
    fecha_inicio DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    condiciones TEXT,
    resolucion TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
