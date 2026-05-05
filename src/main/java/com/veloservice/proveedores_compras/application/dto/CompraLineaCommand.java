package com.veloservice.proveedores_compras.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application command for a purchase line.
 */
@Data
@AllArgsConstructor
public class CompraLineaCommand {
    private UUID productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
