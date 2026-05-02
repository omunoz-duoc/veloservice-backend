CREATE TABLE categorias_producto (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    nombre VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(taller_id, nombre)
);

CREATE TABLE proveedores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    nombre VARCHAR(150) NOT NULL,
    rut VARCHAR(20),
    telefono VARCHAR(20),
    email VARCHAR(100),
    direccion VARCHAR(255),
    condicion_pago VARCHAR(50),
    contacto_asignado VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE productos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    categoria_id UUID REFERENCES categorias_producto(id),
    nombre VARCHAR(150) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    marca VARCHAR(100),
    unidad_medida VARCHAR(20),
    precio_costo DECIMAL(10,2) NOT NULL,
    precio_venta DECIMAL(10,2) NOT NULL,
    stock_actual INT NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo INT NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(taller_id, sku)
);

CREATE TABLE compras (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    proveedor_id UUID NOT NULL REFERENCES proveedores(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    numero_factura VARCHAR(50),
    neto DECIMAL(10,2) NOT NULL,
    iva DECIMAL(10,2) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(20) NOT NULL CHECK (estado IN ('Pendiente','Recibida','Anulada')),
    fecha_compra DATE NOT NULL,
    fecha_recepcion DATE,
    notas TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE compra_productos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    compra_id UUID NOT NULL REFERENCES compras(id),
    producto_id UUID NOT NULL REFERENCES productos(id),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL
);

CREATE TABLE movimientos_stock (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    taller_id UUID NOT NULL REFERENCES talleres(id),
    producto_id UUID NOT NULL REFERENCES productos(id),
    orden_id UUID REFERENCES ordenes(id),
    compra_id UUID REFERENCES compras(id),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('egreso','reversion','ingreso','ajuste_inicial')),
    cantidad INT NOT NULL CHECK (cantidad > 0),
    stock_anterior INT NOT NULL,
    stock_posterior INT NOT NULL CHECK (stock_posterior >= 0),
    motivo TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
