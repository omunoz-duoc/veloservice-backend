package com.veloservice.clientes.application.dto;

import java.time.OffsetDateTime;

/**
 * Application-layer result carrying order summary for a customer profile.
 */
public record OrdenResumenResult(
        String numeroOrden,
        String tipoOrden,
        String estadoOrden,
        OffsetDateTime fechaIngreso
) {
}
