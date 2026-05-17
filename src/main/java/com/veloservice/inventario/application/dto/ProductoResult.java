package com.veloservice.inventario.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application result for product queries.
 */
@Data
@Builder
@AllArgsConstructor
public class ProductoResult {
    private UUID id;
    private String nombre;
    private String sku;
    private String marca;
    private UUID categoriaId;
    private String categoriaNombre;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    private Boolean alertaStockBajo;
}
