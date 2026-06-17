package com.veloservice.finanzas.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;

public record RentabilidadResponse(
        BigDecimal ingresos,
        BigDecimal costos,
        BigDecimal margen,
        long cantidadCobros,
        BigDecimal ticketPromedio,
        List<PuntoRentabilidad> historico
) {
    public record PuntoRentabilidad(
            String periodo,
            BigDecimal ingresos,
            BigDecimal costos,
            long cantidadCobros
    ) {}
}
