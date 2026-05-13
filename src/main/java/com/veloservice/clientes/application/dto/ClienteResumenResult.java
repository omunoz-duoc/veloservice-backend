package com.veloservice.clientes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Result DTO for client summary query containing aggregated data.
 * Includes client info, bike count, order count, and total spending.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResumenResult {
    private String externalId;
    private String nombreCompleto;
    private String rut;
    private String telefono;
    private String email;
    private Long numeroBicicletas;
    private Long numeroOrdenes;
    private BigDecimal gastoTotal;
}
