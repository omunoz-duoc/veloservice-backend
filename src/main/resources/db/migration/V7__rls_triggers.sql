CREATE OR REPLACE FUNCTION set_tenant_context(p_sucursal_id UUID)
RETURNS VOID LANGUAGE plpgsql AS $$
BEGIN
    PERFORM set_config('app.current_sucursal_id', p_sucursal_id::TEXT, FALSE);
END;
$$;

ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE ordenes ENABLE ROW LEVEL SECURITY;
ALTER TABLE orden_estados ENABLE ROW LEVEL SECURITY;
ALTER TABLE orden_servicios ENABLE ROW LEVEL SECURITY;
ALTER TABLE orden_productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE sucursal_clientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE sucursal_servicios ENABLE ROW LEVEL SECURITY;
ALTER TABLE sucursal_proveedores ENABLE ROW LEVEL SECURITY;
ALTER TABLE compras ENABLE ROW LEVEL SECURITY;
ALTER TABLE compra_productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE movimientos_stock ENABLE ROW LEVEL SECURITY;
ALTER TABLE garantias ENABLE ROW LEVEL SECURITY;
ALTER TABLE multimedia ENABLE ROW LEVEL SECURITY;
ALTER TABLE notificaciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE cobros ENABLE ROW LEVEL SECURITY;

CREATE POLICY tenant_isolation_usuarios ON usuarios FOR ALL TO postgres
    USING (sucursal_id = current_setting('app.current_sucursal_id')::UUID);

CREATE POLICY tenant_isolation_productos ON productos FOR ALL TO postgres
    USING (sucursal_id = current_setting('app.current_sucursal_id')::UUID);

CREATE POLICY tenant_isolation_ordenes ON ordenes FOR ALL TO postgres
    USING (sucursal_id = current_setting('app.current_sucursal_id')::UUID);

CREATE POLICY tenant_isolation_orden_estados ON orden_estados FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM ordenes o WHERE o.id = orden_estados.orden_id AND o.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_orden_servicios ON orden_servicios FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM ordenes o WHERE o.id = orden_servicios.orden_id AND o.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_orden_productos ON orden_productos FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM ordenes o WHERE o.id = orden_productos.orden_id AND o.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_sucursal_clientes ON sucursal_clientes FOR ALL TO postgres
    USING (sucursal_id = current_setting('app.current_sucursal_id')::UUID);

CREATE POLICY tenant_isolation_sucursal_servicios ON sucursal_servicios FOR ALL TO postgres
    USING (sucursal_id = current_setting('app.current_sucursal_id')::UUID);

CREATE POLICY tenant_isolation_sucursal_proveedores ON sucursal_proveedores FOR ALL TO postgres
    USING (sucursal_id = current_setting('app.current_sucursal_id')::UUID);

CREATE POLICY tenant_isolation_compras ON compras FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM sucursal_proveedores sp WHERE sp.id = compras.sucursal_proveedor_id AND sp.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_compra_productos ON compra_productos FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM compras c JOIN sucursal_proveedores sp ON sp.id = c.sucursal_proveedor_id WHERE c.id = compra_productos.compra_id AND sp.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_movimientos_stock ON movimientos_stock FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM productos p WHERE p.id = movimientos_stock.producto_id AND p.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_garantias ON garantias FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM ordenes o WHERE o.id = garantias.orden_id AND o.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_multimedia ON multimedia FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM ordenes o WHERE o.id = multimedia.orden_id AND o.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_notificaciones ON notificaciones FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM ordenes o WHERE o.id = notificaciones.orden_id AND o.sucursal_id = current_setting('app.current_sucursal_id')::UUID));

CREATE POLICY tenant_isolation_cobros ON cobros FOR ALL TO postgres
    USING (EXISTS (SELECT 1 FROM ordenes o WHERE o.id = cobros.orden_id AND o.sucursal_id = current_setting('app.current_sucursal_id')::UUID));
