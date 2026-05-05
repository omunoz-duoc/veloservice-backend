package com.veloservice.ordenes.interfaces.rest;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Work order product line request payload.
 */
@Data
public class OrdenProductoRequest {
    @NotNull
    private UUID productoId;
    @NotNull
    @Min(1)
    private Integer cantidad;
    private Boolean proporcionadoPorCliente = false;
    private String notas;
}