package com.veloservice.ordenes.interfaces.rest.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrdenServicioCambioRequest {
    private String accion;
    private UUID servicioId;
    private UUID lineaId;
    private BigDecimal precioAplicado;
    private BigDecimal descuentoAplicado;
    private String notas;
}
