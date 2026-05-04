package com.veloservice.catalogo.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

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
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    private Boolean alertaStockBajo;
}