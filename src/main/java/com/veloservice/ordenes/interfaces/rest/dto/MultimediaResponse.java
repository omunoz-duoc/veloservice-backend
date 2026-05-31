package com.veloservice.ordenes.interfaces.rest.dto;

import com.veloservice.ordenes.domain.EtapaMultimediaEnum;
import com.veloservice.ordenes.domain.TipoArchivoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Multimedia response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class MultimediaResponse {
    private UUID id;
    private UUID ordenId;
    private UUID usuarioId;
    private String url;
    private TipoArchivoEnum tipoArchivo;
    private EtapaMultimediaEnum etapa;
    private String descripcion;
    private OffsetDateTime createdAt;
}
