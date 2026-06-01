package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Work order service line request payload.
 */
@Data
public class OrdenServicioAddRequest {
    @NotNull
    private UUID ordenId;
    @NotNull
    private UUID servicioId;
    private String notas;
}