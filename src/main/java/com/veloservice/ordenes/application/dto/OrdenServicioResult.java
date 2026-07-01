package com.veloservice.ordenes.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenServicioResult(
        UUID id,
        UUID servicioId,
        String nombre,
        BigDecimal precioBase,
        BigDecimal precioAplicado,
        BigDecimal descuentoAplicado,
        String notas,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
) {
    public OrdenServicioResult(
            UUID id,
            UUID servicioId,
            String nombre,
            BigDecimal precioBase,
            BigDecimal precioAplicado,
            BigDecimal descuentoAplicado,
            String notas
    ) {
        this(id, servicioId, nombre, precioBase, precioAplicado, descuentoAplicado, notas,
                null, "Sistema", null);
    }
}
