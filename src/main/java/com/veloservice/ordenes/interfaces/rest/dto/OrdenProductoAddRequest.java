package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * DTO para representar un producto asociado a una orden, utilizado en la creación y actualización de órdenes.
 */
@Data
public class OrdenProductoAddRequest {
    @NotNull
    private UUID productoId;
    @NotNull
    @Min(1)
    private Integer cantidad;
    private Boolean proporcionadoPorCliente = false;
    @Size(max = 1000)
    private String notas;
}
