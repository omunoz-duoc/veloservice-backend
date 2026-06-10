package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ComentarioResult(
    UUID id,
    UUID usuarioId,
    String usuario, 
    String texto, 
    OffsetDateTime createdAt
) {
    public ComentarioResult(String usuario, String texto, OffsetDateTime createdAt) {
        this(null, null, usuario, texto, createdAt);
    }
}
