package com.veloservice.inventario.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class InventarioMetricasResponse {
    @JsonProperty("valor_inventario")
    private long valorInventario;
    @JsonProperty("en_stock")
    private long enStock;
    @JsonProperty("stock_bajo")
    private long stockBajo;
    private long agotados;
}
