INSERT INTO estados_orden (id, codigo, nombre, orden, es_final)
VALUES
    ('30000000-0000-4000-8000-000000000001', 'recibida', 'Recibida', 1, FALSE),
    ('30000000-0000-4000-8000-000000000002', 'en_diagnostico', 'En diagnostico', 2, FALSE),
    ('30000000-0000-4000-8000-000000000003', 'esperando_repuestos', 'Esperando repuestos', 3, FALSE),
    ('30000000-0000-4000-8000-000000000004', 'en_reparacion', 'En reparacion', 4, FALSE),
    ('30000000-0000-4000-8000-000000000005', 'control_calidad', 'Control de calidad', 5, FALSE),
    ('30000000-0000-4000-8000-000000000006', 'lista_para_entrega', 'Lista para entrega', 6, FALSE),
    ('30000000-0000-4000-8000-000000000007', 'entregada', 'Entregada', 7, TRUE),
    ('30000000-0000-4000-8000-000000000008', 'cancelada', 'Cancelada', 8, TRUE)
ON CONFLICT (codigo) DO NOTHING;

INSERT INTO tipos_orden (id, codigo, nombre, activo)
VALUES
    ('31000000-0000-4000-8000-000000000001', 'mantencion', 'Mantencion', TRUE),
    ('31000000-0000-4000-8000-000000000002', 'reparacion', 'Reparacion', TRUE),
    ('31000000-0000-4000-8000-000000000003', 'revision', 'Revision', TRUE),
    ('31000000-0000-4000-8000-000000000004', 'armado', 'Armado', TRUE),
    ('31000000-0000-4000-8000-000000000005', 'garantia', 'Garantia', TRUE),
    ('31000000-0000-4000-8000-000000000006', 'personalizacion', 'Personalizacion', TRUE)
ON CONFLICT (codigo) DO NOTHING;
