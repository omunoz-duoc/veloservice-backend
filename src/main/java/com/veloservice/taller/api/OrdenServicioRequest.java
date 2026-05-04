package com.veloservice.taller.api;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Work order service line request payload.
 */
@Data
public class OrdenServicioRequest {
    @NotNull
    private UUID servicioId;
    private String notas;
}