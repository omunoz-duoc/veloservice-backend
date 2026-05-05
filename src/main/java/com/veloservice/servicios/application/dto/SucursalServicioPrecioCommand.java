package com.veloservice.servicios.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application command for assigning a branch-specific price.
 */
@Data
@AllArgsConstructor
public class SucursalServicioPrecioCommand {
    private UUID servicioId;
    private BigDecimal precioPersonalizado;
}
