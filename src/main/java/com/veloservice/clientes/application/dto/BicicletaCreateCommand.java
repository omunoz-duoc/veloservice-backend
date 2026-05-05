package com.veloservice.clientes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application command for creating a bike.
 */
@Data
@AllArgsConstructor
public class BicicletaCreateCommand {
    private String marca;
    private String modelo;
    private String tipo;
    private String aro;
    private String color;
    private String numeroSerie;
    private Integer anio;
    private String notas;
}
