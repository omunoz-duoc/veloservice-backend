package com.veloservice.ordenes.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OrdenServicioResult {
    private UUID id;
    private UUID servicioId;
    private String nombre;
    private BigDecimal precioBase;
}
