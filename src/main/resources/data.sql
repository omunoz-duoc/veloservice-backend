INSERT INTO talleres (id, nombre, rut, telefono, email, plan_saas, logo_url, activo, created_at, updated_at)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Taller Demo', '76.123.456-7', '+56912345678', 'taller@veloservice.cl', 'basico', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sucursales (id, taller_id, nombre, direccion, telefono, email, activo, created_at, updated_at)
VALUES ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 'Sucursal Centro', 'Av. Principal 123', '+56912345678', 'centro@veloservice.cl', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO roles (id, nombre, descripcion, activo)
VALUES ('770e8400-e29b-41d4-a716-446655440002', 'ADMIN_SUCURSAL', 'Administrador de sucursal', TRUE);

INSERT INTO usuarios (id, sucursal_id, rol_id, nombre, apellido, rut, email, telefono, password_hash, activo, last_login, created_at)
VALUES ('880e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440002', 'Oscar', 'Munoz', '12.345.678-9', 'admin@veloservice.cl', '+56998765432', '$2b$10$instik2q6t5EwB075oyTT.VP99/SaUbdTsAp.xJjpKsyBWUX8Q1hm', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO categorias_producto (id, nombre, descripcion, activo)
VALUES ('d0f3a4b5-c6d7-e8f9-a0b1-c2d3e4f5a6b7', 'Frenos', NULL, TRUE);
INSERT INTO categorias_producto (id, nombre, descripcion, activo)
VALUES ('d1a4b5c6-d7e8-f9a0-b1c2-d3e4f5a6b7c8', 'Transmision', NULL, TRUE);
INSERT INTO categorias_producto (id, nombre, descripcion, activo)
VALUES ('d2b5c6d7-e8f9-a0b1-c2d3-e4f5a6b7c8d9', 'Suspension', NULL, TRUE);
INSERT INTO categorias_producto (id, nombre, descripcion, activo)
VALUES ('d3c6d7e8-f9a0-b1c2-d3e4-f5a6b7c8d9e0', 'Ruedas', NULL, TRUE);
INSERT INTO categorias_producto (id, nombre, descripcion, activo)
VALUES ('d4d7e8f9-a0b1-c2d3-e4f5-a6b7c8d9e0f1', 'Componentes', NULL, TRUE);
INSERT INTO categorias_producto (id, nombre, descripcion, activo)
VALUES ('d5e8f9a0-b1c2-d3e4-f5a6-b7c8d9e0f1a2', 'Lubricantes', NULL, TRUE);
INSERT INTO categorias_producto (id, nombre, descripcion, activo)
VALUES ('d6f9a0b1-c2d3-e4f5-a6b7-c8d9e0f1a2b3', 'Herramientas', NULL, TRUE);
