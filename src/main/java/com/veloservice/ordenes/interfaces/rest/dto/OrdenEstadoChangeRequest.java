package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Work order state change payload.
 */
@Data
public class OrdenEstadoChangeRequest {
    @NotBlank
    private String codigo;

    private String observacion;
}
