package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrdenProductoUpdateRequest {
    @NotNull
    private UUID id;
    @Min(1)
    private Integer cantidad;
    private Boolean proporcionadoPorCliente;
    private String notas;
}
