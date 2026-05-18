package com.veloservice.ordenes.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComentarioRequest {
    @NotBlank
    private String texto;
}
