package com.veloservice.clientes.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Customer summary response payload containing aggregated data.
 * Includes customer info, bike count, order count, and total spending.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResumenResponse {
    private String externalId;
    private String nombreCompleto;
    private String rut;
    private String telefono;
    private String email;
    private Long numeroBicicletas;
    private Long numeroOrdenes;
    private BigDecimal gastoTotal;
}
