package com.veloservice.ordenes.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrdenServicioResponse(
        UUID id,
        UUID servicioId,
        String nombre,
        BigDecimal precioBase,
        BigDecimal precioAplicado,
        BigDecimal descuentoAplicado,
        String notas
) {}
