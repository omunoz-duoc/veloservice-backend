package com.veloservice.ordenes.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class DashboardHoyResponse {
    private long ordenesRecibidas;
    private long ordenesEntregadas;
    private BigDecimal ingresosHoy;
}
