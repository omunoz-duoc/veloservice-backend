package com.veloservice.proveedores_compras.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application result for purchase lines.
 */
@Data
@Builder
@AllArgsConstructor
public class CompraLineaResult {
    private UUID productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
