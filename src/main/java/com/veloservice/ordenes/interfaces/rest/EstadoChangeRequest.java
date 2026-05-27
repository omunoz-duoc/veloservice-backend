package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Work order state change payload.
 */
@Data
public class EstadoChangeRequest {
    @NotNull
    private EstadoOrdenEnum nuevoEstado;
    @NotBlank
    private String observacion;
}