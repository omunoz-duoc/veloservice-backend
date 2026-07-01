package com.veloservice.ordenes.interfaces.rest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenMultimediaResponse(
        UUID id,
        UUID usuarioId,
        String usuario,
        String tipoArchivo,
        String categoria,
        String url,
        String etapa,
        String descripcion,
        OffsetDateTime createdAt
) {
}
