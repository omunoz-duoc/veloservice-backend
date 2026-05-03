package com.bikeshop.manager.application.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Membership creation request payload.
 */
@Data
public class MembresiaRequest {
    @NotBlank
    private String nombre;
    private String descripcion;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal porcentajeDescuento;
    @Min(0)
    private Integer prioridadAtencion;
    private String colorBadge;
    private Boolean activo;
}
