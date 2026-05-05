package com.veloservice.clientes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Application result for the current membership.
 */
@Data
@Builder
@AllArgsConstructor
public class MembresiaActualResult {
    private String nombre;
    private BigDecimal descuento;
}
