package com.veloservice.servicios.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class ServicioResponse {
    private UUID id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioBase;
    private Boolean activo;
}