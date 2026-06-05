package com.veloservice.ordenes.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrdenServicioResult(
        UUID id,
        UUID servicioId,
        String nombre,
        BigDecimal precioBase,
        BigDecimal precioAplicado,
        BigDecimal descuentoAplicado,
        String notas
) {}
