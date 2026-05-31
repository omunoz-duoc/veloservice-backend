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
    @JsonProperty("cobros_del_dia")
    private BigDecimal cobrosDelDia;
}
