package com.veloservice.finanzas.interfaces.rest.dto;

import java.math.BigDecimal;

public record FinanzasHoyResponse(
        BigDecimal totalIngresosHoy,
        BigDecimal deltaVsAyerPorcentaje
) {
}
