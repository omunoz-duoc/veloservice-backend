package com.veloservice.ordenes.interfaces.rest.dto;

import com.veloservice.ordenes.domain.EstadoGarantiaEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Warranty response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class GarantiaResponse {
    private UUID id;
    private UUID ordenId;
    private String numeroGarantia;
    private String marcaBicicleta;
    private String componenteAfectado;
    private String descripcionFalla;
    private EstadoGarantiaEnum estado;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private String condiciones;
    private String resolucion;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
