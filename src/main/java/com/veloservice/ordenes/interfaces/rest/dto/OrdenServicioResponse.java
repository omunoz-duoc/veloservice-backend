package com.veloservice.ordenes.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenServicioResponse(
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
    public OrdenServicioResponse(
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
