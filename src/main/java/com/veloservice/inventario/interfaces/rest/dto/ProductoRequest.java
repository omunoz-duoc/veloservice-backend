package com.veloservice.inventario.interfaces.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product creation request payload.
 */
@Data
public class ProductoRequest {
    @NotBlank
    private String nombre;
    @NotBlank
    private String sku;
    private String marca;
    private String unidadMedida;
    @NotNull
    @Min(0)
    private BigDecimal precioCosto;
    @NotNull
    @Min(0)
    private BigDecimal precioVenta;
    @Min(0)
    private Integer stock = 0;
    @Min(0)
    private Integer stockMinimo = 0;
    private UUID categoriaId;
}
