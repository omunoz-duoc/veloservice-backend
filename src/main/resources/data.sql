INSERT INTO talleres (id, nombre, rut, telefono, email, plan_saas, logo_url, activo, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Taller Demo', '76.123.456-7', '+56912345678', 'taller@veloservice.cl', 'basico', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sucursales (id, taller_id, nombre, direccion, telefono, email, activo, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'Sucursal Centro', 'Av. Principal 123', '+56912345678', 'centro@veloservice.cl', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO roles (id, nombre, descripcion, activo)
VALUES ('770e8400-e29b-41d4-a716-446655440002', 'ADMIN_SUCURSAL', 'Administrador de sucursal', TRUE);

INSERT INTO usuarios (id, sucursal_id, rol_id, nombre, apellido, rut, email, telefono, password_hash, activo, last_login, created_at)
VALUES ('880e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', 'Oscar', 'Munoz', '12.345.678-9', 'admin@veloservice.cl', '+56998765432', '$2b$10$instik2q6t5EwB075oyTT.VP99/SaUbdTsAp.xJjpKsyBWUX8Q1hm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar cliente de prueba
INSERT INTO clientes (id, nombre, apellido, rut, email, telefono, created_at, updated_at)
VALUES ('aa0e8400-e29b-41d4-a716-446655440005', 'Matias', 'Diaz', '13.456.789-0', 'matias@email.com', '+56912345678', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insertar bicicleta de prueba
INSERT INTO bicicletas (id, cliente_id, marca, modelo, numero_serie, color, tipo, created_at)
VALUES ('990e8400-e29b-41d4-a716-446655440004', 'aa0e8400-e29b-41d4-a716-446655440005', 'Trek', 'Domane SL', 'SN123456', 'Rojo', 'Ruta', CURRENT_TIMESTAMP);

-- Insertar orden 1 (EN_PROCESO)
INSERT INTO ordenes (id, numero_orden, sucursal_id, mecanico_id, bicicleta_id, estado, tipo, diagnostico_inicial, fecha_ingreso, fecha_prometida, created_at, updated_at, descuento_manual, porcentaje_descuento_membresia)
VALUES ('bb0e8400-e29b-41d4-a716-446655440006', 'OT-2026-001', '660e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440004', 'en_reparacion', 'MANTENCION', 'Cambio de cadena y ajuste de frenos', CURRENT_TIMESTAMP, DATEADD('DAY', 3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0);

-- Insertar orden 2 (RECIBIDA)
INSERT INTO ordenes (id, numero_orden, sucursal_id, mecanico_id, bicicleta_id, estado, tipo, diagnostico_inicial, fecha_ingreso, fecha_prometida, created_at, updated_at, descuento_manual, porcentaje_descuento_membresia)
VALUES ('cc0e8400-e29b-41d4-a716-446655440007', 'OT-2026-002', '660e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440003', '990e8400-e29b-41d4-a716-446655440004', 'RECIBIDA', 'REPARACION', 'Ruido en transmisión al pedalear', CURRENT_TIMESTAMP, DATEADD('DAY', 2, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, 0);
