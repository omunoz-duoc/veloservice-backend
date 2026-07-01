package com.veloservice.servicios.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application result for branch-service links.
 */
@Data
@Builder
@AllArgsConstructor
public class SucursalServicioResult {
    private UUID id;
    private UUID sucursalId;
    private UUID servicioId;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private BigDecimal precioPersonalizado;
    private BigDecimal precioVigente;
    private Boolean activo;
    private OffsetDateTime createdAt;
}
