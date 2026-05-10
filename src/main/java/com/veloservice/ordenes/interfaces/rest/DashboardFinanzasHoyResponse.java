package com.veloservice.ordenes.interfaces.rest;

import java.math.BigDecimal;

public record DashboardFinanzasHoyResponse(
        BigDecimal totalIngresosHoy,
        long totalCobrosHoy,
        String metodoPagoMasUsado
) {
}
