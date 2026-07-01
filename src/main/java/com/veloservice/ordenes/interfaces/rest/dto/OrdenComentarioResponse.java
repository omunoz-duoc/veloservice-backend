package com.veloservice.ordenes.interfaces.rest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenComentarioResponse(
        UUID id,
        UUID usuarioId,
        String usuario,
        String texto,
        OffsetDateTime createdAt
) {
}
