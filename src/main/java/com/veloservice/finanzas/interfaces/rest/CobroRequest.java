package com.veloservice.finanzas.interfaces.rest;

import jakarta.validation.constraints.DecimalMin;
import com.veloservice.config.enums.MetodoPagoEnum;
import com.veloservice.config.enums.TipoDocumentoEnum;
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
    @NotNull
    private TipoDocumentoEnum tipoDocumento;
    @NotBlank
    private String numeroDocumento;
    @NotNull
    private MetodoPagoEnum metodoPago;
    @DecimalMin("0.0")
    private BigDecimal descuentoManual;
}