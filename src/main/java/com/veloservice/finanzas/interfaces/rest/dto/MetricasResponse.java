package com.veloservice.finanzas.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class MetricasResponse {
    @JsonProperty("cobrosDelDia")
    private BigDecimal cobrosDelDia;

    @JsonProperty("cantidadCobrosDia")
    private long cantidadCobrosDia;

    @JsonProperty("cobros_del_dia")
    public BigDecimal getCobrosDelDiaLegacy() {
        return cobrosDelDia;
    }

    @JsonProperty("cantidad_cobros_dia")
    public long getCantidadCobrosDiaLegacy() {
        return cantidadCobrosDia;
    }
}
