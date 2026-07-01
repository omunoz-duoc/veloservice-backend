package com.veloservice.ordenes.interfaces.rest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenHistorialResponse(
        UUID id,
        String accion,
        String entidad,
        UUID entidadId,
        String detalle,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
) {}
