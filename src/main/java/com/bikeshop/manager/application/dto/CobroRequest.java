package com.bikeshop.manager.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Settlement creation request payload.
 */
@Data
public class CobroRequest {
    @NotNull
    private UUID ordenId;
    @NotBlank
    private String tipoDocumento;
    @NotBlank
    private String numeroDocumento;
    @NotBlank
    private String metodoPago;
    @DecimalMin("0.0")
    private BigDecimal descuentoManual;
}
