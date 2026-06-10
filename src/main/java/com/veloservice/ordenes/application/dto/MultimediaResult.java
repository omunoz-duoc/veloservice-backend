package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MultimediaResult(
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
    public MultimediaResult(
            String usuario,
            String tipoArchivo,
            String url,
            String etapa,
            String descripcion,
            OffsetDateTime createdAt
    ) {
        this(null, null, usuario, tipoArchivo, null, url, etapa, descripcion, createdAt);
    }
}
