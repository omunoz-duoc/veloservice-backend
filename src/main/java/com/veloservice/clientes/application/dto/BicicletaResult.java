package com.veloservice.clientes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Application result for bike queries.
 */
@Data
@Builder
@AllArgsConstructor
public class BicicletaResult {
    private UUID id;
    private UUID clienteId;
    private String marca;
    private String modelo;
    private String tipo;
    private String aro;
    private String color;
    private String numeroSerie;
    private Integer anio;
}
