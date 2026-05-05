package com.veloservice.ordenes.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Warranty creation request payload.
 */
@Data
public class GarantiaRequest {
    @NotNull
    private UUID ordenProductoId;
    @NotNull
    private UUID ordenId;
    @NotBlank
    private String numeroGarantia;
    @NotBlank
    private String descripcionFalla;
    @NotNull
    private LocalDate fechaInicio;
    @NotNull
    private LocalDate fechaVencimiento;
    private String condiciones;
    private String resolucion;
}