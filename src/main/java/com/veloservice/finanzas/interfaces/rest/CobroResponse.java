package com.veloservice.finanzas.interfaces.rest;

import com.veloservice.finanzas.domain.EstadoCobroEnum;
import com.veloservice.finanzas.domain.EstadoSIIEnum;
import com.veloservice.finanzas.domain.MetodoPagoEnum;
import com.veloservice.finanzas.domain.TipoDocumentoEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Settlement response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class CobroResponse {
    private UUID id;
    private UUID ordenId;
    private UUID usuarioId;
    private TipoDocumentoEnum tipoDocumento;
    private String numeroDocumento;
    private BigDecimal subtotalServicios;
    private BigDecimal subtotalProductos;
    private BigDecimal descuentoMembresia;
    private BigDecimal descuentoManual;
    private BigDecimal neto;
    private BigDecimal iva;
    private BigDecimal total;
    private MetodoPagoEnum metodoPago;
    private EstadoCobroEnum estado;
    private String folioSii;
    private EstadoSIIEnum estadoSii;
    private OffsetDateTime fechaPago;
    private OffsetDateTime anuladaAt;
    private String motivoAnulacion;
    private OffsetDateTime createdAt;
}
