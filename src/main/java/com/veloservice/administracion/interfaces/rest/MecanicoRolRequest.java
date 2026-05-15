package com.veloservice.administracion.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MecanicoRolRequest {
    @NotBlank
    private String rol;
}
