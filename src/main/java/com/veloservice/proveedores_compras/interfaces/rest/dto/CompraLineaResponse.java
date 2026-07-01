package com.veloservice.proveedores_compras.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Purchase line response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class CompraLineaResponse {
    private UUID productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}