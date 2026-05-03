package com.bikeshop.manager.application.dto;

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
