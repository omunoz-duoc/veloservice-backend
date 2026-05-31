package com.veloservice.clientes.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Bike response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class BicicletaResponse {
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
