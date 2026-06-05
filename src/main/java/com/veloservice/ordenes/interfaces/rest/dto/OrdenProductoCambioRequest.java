package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.UUID;

@Data
public class OrdenProductoCambioRequest {
    private String accion;
    private UUID productoId;
    private UUID lineaId;
    @Min(1)
    private Integer cantidad;
    private String notas;
    private Boolean proporcionadoPorCliente;
}
