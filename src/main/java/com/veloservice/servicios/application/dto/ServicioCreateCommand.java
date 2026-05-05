package com.veloservice.servicios.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Application command for creating or updating a service.
 */
@Data
@AllArgsConstructor
public class ServicioCreateCommand {
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private Boolean activo;
}
