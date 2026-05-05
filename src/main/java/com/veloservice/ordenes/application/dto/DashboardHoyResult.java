package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Application result for daily dashboard metrics.
 */
@Data
@Builder
@AllArgsConstructor
public class DashboardHoyResult {
    private long ordenesRecibidas;
    private long ordenesEntregadas;
    private BigDecimal ingresosHoy;
}
