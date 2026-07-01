package com.veloservice.clientes.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Current membership summary for a customer.
 */
@Data
@Builder
@AllArgsConstructor
public class MembresiaActualResponse {
    private String nombre;
    private BigDecimal descuento;
}
