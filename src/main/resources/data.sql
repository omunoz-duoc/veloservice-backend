INSERT INTO talleres (id, nombre, rut, telefono, email, plan_saas, logo_url, activo, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Taller Demo', '76.123.456-7', '+56912345678', 'taller@veloservice.cl', 'basico', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sucursales (id, taller_id, nombre, direccion, telefono, email, activo, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'Sucursal Centro', 'Av. Principal 123', '+56912345678', 'centro@veloservice.cl', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO roles (id, nombre, descripcion, activo)
VALUES ('770e8400-e29b-41d4-a716-446655440002', 'ADMIN_SUCURSAL', 'Administrador de sucursal', TRUE);

INSERT INTO usuarios (id, sucursal_id, rol_id, nombre, apellido, rut, email, telefono, password_hash, activo, last_login, created_at)
VALUES ('880e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', 'Oscar', 'Munoz', '12.345.678-9', 'admin@veloservice.cl', '+56998765432', '$2b$10$instik2q6t5EwB075oyTT.VP99/SaUbdTsAp.xJjpKsyBWUX8Q1hm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
