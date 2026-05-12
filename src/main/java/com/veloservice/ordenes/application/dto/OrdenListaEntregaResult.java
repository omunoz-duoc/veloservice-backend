package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.EstadoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application result for orders ready for delivery.
 */
@Value
@Builder
@AllArgsConstructor
public class OrdenListaEntregaResult {
    UUID id;
    String numeroOrden;
    String clienteNombre;
    String mecanicoAsignado;
    OffsetDateTime fechaIngreso;
    EstadoOrdenEnum estado;
    BigDecimal totalEstimado;
}