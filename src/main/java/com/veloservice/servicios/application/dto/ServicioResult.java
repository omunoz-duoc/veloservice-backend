package com.veloservice.servicios.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application result for service queries.
 */
@Data
@Builder
@AllArgsConstructor
public class ServicioResult {
    private UUID id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private Boolean activo;
}
