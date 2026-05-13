package com.veloservice.clientes.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Customer search response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class ClienteBusquedaResponse {
    private UUID id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String rut;
}
