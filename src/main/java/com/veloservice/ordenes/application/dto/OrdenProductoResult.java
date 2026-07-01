package com.veloservice.ordenes.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenProductoResult(
        UUID id,
        UUID productoId,
        String nombre,
        String sku,
        Integer cantidad,
        BigDecimal precioVenta,
        BigDecimal precioAplicado,
        String notas,
        Boolean proporcionadoPorCliente,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
) {
    public OrdenProductoResult(
            UUID id,
            UUID productoId,
            String nombre,
            String sku,
            Integer cantidad,
            BigDecimal precioVenta,
            BigDecimal precioAplicado,
            String notas,
            Boolean proporcionadoPorCliente
    ) {
        this(id, productoId, nombre, sku, cantidad, precioVenta, precioAplicado, notas,
                proporcionadoPorCliente, null, "Sistema", null);
    }
}
