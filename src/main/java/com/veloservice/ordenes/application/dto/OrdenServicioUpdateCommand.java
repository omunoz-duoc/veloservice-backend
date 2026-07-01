package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class OrdenServicioUpdateCommand {
    private UUID id;
    private BigDecimal precioAplicado;
    private BigDecimal descuentoAplicado;
    private String notas;
}
