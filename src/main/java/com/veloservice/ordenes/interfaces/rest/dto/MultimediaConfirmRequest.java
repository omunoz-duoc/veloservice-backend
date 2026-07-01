package com.veloservice.ordenes.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MultimediaConfirmRequest(
        @NotBlank String objectKey,
        @NotBlank @Size(max = 2048) String publicUrl,
        @NotBlank String tipoArchivo,
        @Size(max = 500) String descripcion,
        String etapa
) {
}
