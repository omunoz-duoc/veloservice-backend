INSERT INTO talleres (id, nombre, rut, telefono, email, plan_saas, logo_url, activo, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Taller Demo', '76.123.456-7', '+56912345678', 'taller@veloservice.cl', 'basico', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sucursales (id, taller_id, nombre, direccion, telefono, email, activo, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'Sucursal Centro', 'Av. Principal 123', '+56912345678', 'centro@veloservice.cl', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO roles (id, nombre, descripcion, activo)
VALUES ('770e8400-e29b-41d4-a716-446655440002', 'ADMIN_SUCURSAL', 'Administrador de sucursal', TRUE);

INSERT INTO usuarios (id, sucursal_id, rol_id, nombre, apellido, rut, email, telefono, password_hash, activo, last_login, created_at)
VALUES ('880e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', 'Oscar', 'Munoz', '12.345.678-9', 'admin@veloservice.cl', '+56998765432', '$2b$10$instik2q6t5EwB075oyTT.VP99/SaUbdTsAp.xJjpKsyBWUX8Q1hm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar cliente de prueba
INSERT INTO clientes (id, external_id, nombre, apellido, rut, email, telefono, created_at, updated_at)
VALUES ('aa0e8400-e29b-41d4-a716-446655440005', 'CLI-0001', 'Matias', 'Diaz', '13.456.789-0', 'matias@email.com', '+56912345678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar bicicleta de prueba
INSERT INTO bicicletas (id, cliente_id, marca, modelo, numero_serie, color, tipo, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440005', 'Trek', 'Domane SL', 'SN123456', 'Rojo', 'Ruta', CURRENT_TIMESTAMP);

-- Insertar orden 1 (EN_PROCESO)
INSERT INTO ordenes (id, external_id, numero_orden, sucursal_id, mecanico_id, bicicleta_id, estado, tipo, prioridad, descripcion_trabajo, diagnostico_inicial, fecha_ingreso, fecha_prometida, created_at, updated_at, descuento_manual, porcentaje_descuento_membresia)
VALUES ('bb0e8400-e29b-41d4-a716-446655440006', 'OT-2026-001', 'OT-2026-001', '660e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440004', 'recibida', 'mantencion', 'MEDIA', 'Cambio de cadena y ajuste de frenos', 'Cambio de cadena y ajuste de frenos', CURRENT_TIMESTAMP, DATEADD('DAY', 3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0);

-- Insertar orden 2 (RECIBIDA)
INSERT INTO ordenes (id, external_id, numero_orden, sucursal_id, mecanico_id, bicicleta_id, estado, tipo, prioridad, descripcion_trabajo, diagnostico_inicial, fecha_ingreso, fecha_prometida, created_at, updated_at, descuento_manual, porcentaje_descuento_membresia)
VALUES ('cc0e8400-e29b-41d4-a716-446655440007', 'OT-2026-002', 'OT-2026-002', '660e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440004', 'recibida', 'reparacion', 'MEDIA', 'Revisión de transmisión', 'Ruido en transmisión al pedalear', CURRENT_TIMESTAMP, DATEADD('DAY', 2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0);

INSERT INTO comentarios (id, orden_id, usuario_id, texto, created_at)
VALUES ('d10e8400-e29b-41d4-a716-446655440010', 'bb0e8400-e29b-41d4-a716-446655440006', '880e8400-e29b-41d4-a716-446655440003', 'Se detectó desgaste', CURRENT_TIMESTAMP);

-- Insertar multimedia de prueba para orden 1
INSERT INTO multimedia (id, orden_id, usuario_id, url, tipo_archivo, etapa, descripcion, created_at)
VALUES ('e10e8400-e29b-41d4-a716-446655440011', 'bb0e8400-e29b-41d4-a716-446655440006', '880e8400-e29b-41d4-a716-446655440003', 'https://storage.example.com/foto1.jpg', 'imagen', 'diagnostico', 'Foto de daño', CURRENT_TIMESTAMP);

-- Insertar producto de prueba para orden 1
INSERT INTO orden_productos (id, orden_id, producto_id, cantidad, precio_aplicado, precio_costo_snapshot, precio_venta_snapshot, proporcionado_por_cliente, created_at, notas)
VALUES ('f10e8400-e29b-41d4-a716-446655440012', 'bb0e8400-e29b-41d4-a716-446655440006', 'a10e8400-e29b-41d4-a716-446655440013', 1, 18900, 15000, 18900, FALSE, CURRENT_TIMESTAMP, 'Cadena Shimano HG601');

-- Insertar producto en tabla productos para referencia
INSERT INTO productos (id, sucursal_id, nombre, sku, precio_costo, precio_venta, stock, stock_minimo, unidad_medida, activo, created_at, updated_at)
VALUES ('a10e8400-e29b-41d4-a716-446655440013', '660e8400-e29b-41d4-a716-446655440001', 'Cadena Shimano HG601', 'SHM-HG601-11', 15000, 18900, 10, 2, 'unidad', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
