package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;

/**
 * JPQL projection for customer order summaries used by the clientes context.
 */
public record OrdenResumenClienteResult(
        String numeroOrden,
        String tipoOrden,
        String estadoOrden,
        OffsetDateTime fechaIngreso
) {
}
