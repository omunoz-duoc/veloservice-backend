package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MultimediaPresignRequest(
        @NotBlank String tipoArchivo,
        @NotBlank @Size(max = 255) String nombre
) {
}
