CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    rut VARCHAR(20) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    direccion VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(taller_id, rut)
);

CREATE TABLE bicicletas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    cliente_id UUID NOT NULL REFERENCES clientes(id),
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    tipo VARCHAR(50),
    aro VARCHAR(20),
    color VARCHAR(50),
    numero_serie VARCHAR(100),
    anio INT,
    foto_url VARCHAR(255),
    notas TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE taller_clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    cliente_id UUID NOT NULL REFERENCES clientes(id),
    membresia_id UUID,
    notas TEXT,
    membresia_desde TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(taller_id, cliente_id)
);
