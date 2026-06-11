package com.veloservice.inventario.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Product response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class ProductoResponse {
    private UUID id;
    private String nombre;
    private String sku;
    private String marca;
    @JsonProperty("categoria")
    private String categoria;
    @JsonProperty("costo_unitario")
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;

    @JsonProperty("precio_asignado")
    public BigDecimal getPrecioAsignado() {
        return precioVenta;
    }

    @JsonProperty("stock_minimo")
    public Integer getStockMinimoJson() {
        return stockMinimo;
    }
}
