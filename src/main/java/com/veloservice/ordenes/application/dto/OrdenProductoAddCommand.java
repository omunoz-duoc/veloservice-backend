package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Application command for adding a product to a work order.
 */
@Data
@AllArgsConstructor
public class OrdenProductoAddCommand {
    private UUID productoId;
    private Integer cantidad;
    private Boolean proporcionadoPorCliente;
    private String notas;
}
