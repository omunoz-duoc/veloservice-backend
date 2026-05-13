package com.veloservice.ordenes.interfaces.rest;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenActividadRecienteResponse(
        UUID idOrden,
        String tipoActividad,
        OffsetDateTime fechaHora,
        String descripcion,
        String usuarioResponsable
) {
}