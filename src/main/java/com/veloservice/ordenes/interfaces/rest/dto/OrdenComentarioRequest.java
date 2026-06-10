package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrdenComentarioRequest(
        @NotBlank
        @Size(max = 4000)
        String texto
) {
}
