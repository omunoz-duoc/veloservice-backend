package com.veloservice.clientes.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Bike response payload for customer listing.
 */
@Data
@Builder
@AllArgsConstructor
public class BicicletaClienteResponse {
    private UUID id;
    private String marcaModelo;
    private String tipo;
    private String talla;
    private String color;
    private String numeroSerie;
}
