package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenActividadRecienteResult(
        UUID idOrden,
        String tipoActividad,
        OffsetDateTime fechaHora,
        String descripcion,
        String usuarioResponsable
) {
}