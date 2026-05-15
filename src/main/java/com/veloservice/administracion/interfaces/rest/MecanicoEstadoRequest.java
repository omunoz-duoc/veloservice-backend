package com.veloservice.administracion.interfaces.rest;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MecanicoEstadoRequest {
    @NotNull
    private Boolean activo;
}
