package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.enums.TipoArchivoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application result for multimedia queries.
 */
@Data
@Builder
@AllArgsConstructor
public class MultimediaResult {
    private UUID id;
    private UUID ordenId;
    private UUID usuarioId;
    private String url;
    private TipoArchivoEnum tipoArchivo;
    private EtapaMultimediaEnum etapa;
    private String descripcion;
    private OffsetDateTime createdAt;
}
