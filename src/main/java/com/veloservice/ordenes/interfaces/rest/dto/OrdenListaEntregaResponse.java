package com.veloservice.ordenes.interfaces.rest.dto;

import com.veloservice.ordenes.domain.EstadoOrdenEnum;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenListaEntregaResponse(
        UUID id,
        String numeroOrden,
        String clienteNombre,
        String mecanicoAsignado,
        OffsetDateTime fechaIngreso,
        EstadoOrdenEnum estado,
        BigDecimal totalEstimado
) {
}
