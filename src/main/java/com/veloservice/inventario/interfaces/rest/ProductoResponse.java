package com.veloservice.inventario.interfaces.rest;

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
    @JsonProperty("categoria")
    private String categoria;
    @JsonProperty("costo_unitario")
    private BigDecimal precioCosto;
    @JsonProperty("precio_asignado")
    private BigDecimal precioVenta;
    private Integer stock;
}