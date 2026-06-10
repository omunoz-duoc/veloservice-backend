package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenEstadoResult(
        UUID id,
        UUID estadoAnteriorId,
        String estadoAnteriorCodigo,
        String estadoAnteriorNombre,
        UUID estadoNuevoId,
        String estadoNuevoCodigo,
        String estadoNuevoNombre,
        String observacion,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
) {
}
