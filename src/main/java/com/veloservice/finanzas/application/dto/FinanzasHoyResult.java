package com.veloservice.finanzas.application.dto;

import java.math.BigDecimal;

public record FinanzasHoyResult(
        BigDecimal totalIngresosHoy,
        BigDecimal deltaVsAyerPorcentaje
) {
}
