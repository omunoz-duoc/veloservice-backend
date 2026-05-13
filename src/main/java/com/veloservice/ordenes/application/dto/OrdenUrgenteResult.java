package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.EstadoOrdenEnum;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenUrgenteResult(
        UUID id,
        String numeroOrden,
        String clienteNombre,
        EstadoOrdenEnum estado,
        OffsetDateTime fechaIngreso
) {
}