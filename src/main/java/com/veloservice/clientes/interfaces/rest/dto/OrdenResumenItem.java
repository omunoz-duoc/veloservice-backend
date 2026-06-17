package com.veloservice.clientes.interfaces.rest.dto;

import java.time.OffsetDateTime;

/**
 * REST DTO for an order summary item inside the customer detail response.
 */
public record OrdenResumenItem(
        String numeroOrden,
        String tipoOrden,
        String estadoOrden,
        OffsetDateTime fechaIngreso
) {
}
