package com.bikeshop.manager.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Multimedia evidence payload.
 */
@Data
public class MultimediaRequest {
    @NotBlank
    private String url;
    @NotBlank
    private String tipoArchivo;
    private String descripcion;
}
