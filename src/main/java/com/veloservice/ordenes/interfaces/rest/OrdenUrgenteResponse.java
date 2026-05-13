package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.config.enums.EstadoOrdenEnum;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenUrgenteResponse(
        UUID id,
        String numeroOrden,
        String clienteNombre,
        EstadoOrdenEnum estado,
        OffsetDateTime fechaIngreso,
        long diasSinMovimiento,
        Integer prioridad
) {
}