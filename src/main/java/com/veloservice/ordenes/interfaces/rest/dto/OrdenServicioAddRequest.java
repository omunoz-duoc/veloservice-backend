package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Work order service line request payload.
 */
@Data
public class OrdenServicioAddRequest {
    @NotNull
    private UUID servicioId;
    @Size(max = 1000)
    private String notas;
}
