package com.veloservice.catalogo.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Service creation request payload.
 */
@Data
public class ServicioRequest {
    @NotBlank
    private String nombre;
    private String descripcion;
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal precioBase;
    private Boolean activo;
}