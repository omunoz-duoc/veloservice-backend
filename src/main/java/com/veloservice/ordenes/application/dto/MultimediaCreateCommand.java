package com.veloservice.ordenes.application.dto;

import com.veloservice.ordenes.domain.TipoArchivoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application command for uploading multimedia evidence.
 */
@Data
@AllArgsConstructor
public class MultimediaCreateCommand {
    private String url;
    private TipoArchivoEnum tipoArchivo;
    private String descripcion;
}
