package com.bikeshop.manager.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Work order state change payload.
 */
@Data
public class EstadoChangeRequest {
    @NotBlank
    private String nuevoEstado;
    @NotBlank
    private String observacion;
}
