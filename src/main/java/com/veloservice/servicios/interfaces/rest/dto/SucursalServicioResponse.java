package com.veloservice.servicios.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Branch-service link response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class SucursalServicioResponse {
    private UUID id;
    private UUID sucursalId;
    private UUID servicioId;
    private BigDecimal precioPersonalizado;
    private Boolean activo;
    private OffsetDateTime createdAt;
}
