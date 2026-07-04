package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.domain.TipoArchivoEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConfirmRequest {
    @NotBlank
    private String fileKey;
    @NotNull
    private TipoArchivoEnum tipoArchivo;
    private String descripcion;
}
