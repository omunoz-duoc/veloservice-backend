package com.veloservice.finanzas.application.dto;

import com.veloservice.finanzas.domain.MetodoPagoEnum;
import com.veloservice.finanzas.domain.TipoDocumentoEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application command for creating a settlement.
 */
@Data
@AllArgsConstructor
public class CobroCreateCommand {
    private UUID ordenId;
    private TipoDocumentoEnum tipoDocumento;
    private String numeroDocumento;
    private MetodoPagoEnum metodoPago;
    private BigDecimal descuentoManual;
}
