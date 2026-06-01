package com.veloservice.ordenes.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrdenProductoResult(
        UUID id,
        UUID productoId,
        String nombre,
        String sku,
        Integer cantidad,
        BigDecimal precioVenta
) {}
