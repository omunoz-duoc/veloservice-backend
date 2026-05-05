package com.veloservice.clientes.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Bike creation request payload.
 */
@Data
public class BicicletaRequest {
    @NotBlank
    private String marca;
    @NotBlank
    private String modelo;
    private String tipo;
    private String aro;
    private String color;
    private String numeroSerie;
    private Integer anio;
    private String notas;
}