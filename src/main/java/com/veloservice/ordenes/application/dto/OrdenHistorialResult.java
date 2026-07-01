package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenHistorialResult(
        UUID id,
        UUID ordenId,
        String accion,
        String entidad,
        UUID entidadId,
        String detalle,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
) {}
