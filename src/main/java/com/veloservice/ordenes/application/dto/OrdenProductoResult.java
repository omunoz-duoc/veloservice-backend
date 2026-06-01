package com.veloservice.ordenes.application.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class OrdenProductoResult {
    private UUID id;
    private UUID productoId;
    private String nombre;
    private String sku;
    private Integer cantidad;
    private BigDecimal precioVenta;
}
