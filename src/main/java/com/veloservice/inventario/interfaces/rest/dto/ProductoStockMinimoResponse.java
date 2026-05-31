package com.veloservice.inventario.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product response for minimum stock alerts without cost fields.
 */
@Data
@Builder
@AllArgsConstructor
public class ProductoStockMinimoResponse {
    private UUID id;
    private String nombre;
    private String sku;
    private String marca;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    private Boolean alertaStockBajo;
}
