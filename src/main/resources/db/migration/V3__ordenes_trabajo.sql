CREATE TABLE ordenes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    bicicleta_id UUID NOT NULL REFERENCES bicicletas(id),
    mecanico_id UUID NOT NULL REFERENCES usuarios(id),
    numero_orden VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('Recibido','En_Proceso','Listo','Entregado','Cancelado')),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('Reparacion','Mantenimiento','Personalizacion')),
    diagnostico_inicial TEXT,
    diagnostico_final TEXT,
    observaciones_cliente TEXT,
    descuento_manual DECIMAL(10,2) NOT NULL DEFAULT 0,
    porcentaje_descuento_membresia DECIMAL(5,2) NOT NULL DEFAULT 0,
    fecha_ingreso TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_prometida TIMESTAMP,
    fecha_entrega TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(taller_id, numero_orden)
);

CREATE TABLE orden_estados (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    orden_id UUID NOT NULL REFERENCES ordenes(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    estado_anterior VARCHAR(20) NOT NULL,
    estado_nuevo VARCHAR(20) NOT NULL,
    observacion TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE multimedia (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    orden_id UUID NOT NULL REFERENCES ordenes(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    url VARCHAR(500) NOT NULL,
    tipo_archivo VARCHAR(20) NOT NULL,
    etapa VARCHAR(20) NOT NULL CHECK (etapa IN ('recepcion','diagnostico','reparacion','entrega')),
    descripcion TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE secuencias_taller (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    tipo VARCHAR(10) NOT NULL,
    anio INT NOT NULL,
    ultimo_numero INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(taller_id, tipo, anio)
);
