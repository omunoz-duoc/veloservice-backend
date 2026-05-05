package com.veloservice.inventario.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application command for creating a product.
 */
@Data
@AllArgsConstructor
public class ProductoCreateCommand {
    private String nombre;
    private String sku;
    private String marca;
    private String unidadMedida;
    private BigDecimal precioCosto;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    private UUID categoriaId;
}
