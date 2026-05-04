package com.veloservice.taller.api;

import com.veloservice.config.enums.TipoArchivoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Multimedia evidence payload.
 */
@Data
public class MultimediaRequest {
    @NotBlank
    private String url;
    @NotNull
    private TipoArchivoEnum tipoArchivo;
    private String descripcion;
}