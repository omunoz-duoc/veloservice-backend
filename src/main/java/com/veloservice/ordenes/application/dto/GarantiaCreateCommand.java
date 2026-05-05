package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Application command for creating a warranty.
 */
@Data
@AllArgsConstructor
public class GarantiaCreateCommand {
    private UUID ordenProductoId;
    private UUID ordenId;
    private String numeroGarantia;
    private String descripcionFalla;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private String condiciones;
    private String resolucion;
}
