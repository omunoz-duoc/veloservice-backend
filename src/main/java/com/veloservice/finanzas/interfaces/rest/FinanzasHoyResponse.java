package com.veloservice.finanzas.interfaces.rest;

import java.math.BigDecimal;

public record FinanzasHoyResponse(
        BigDecimal totalIngresosHoy,
        BigDecimal deltaVsAyerPorcentaje
) {
}
