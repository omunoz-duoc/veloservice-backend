package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComentarioRequest {
    @NotBlank
    private String texto;
}
