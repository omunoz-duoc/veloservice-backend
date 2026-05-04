package com.veloservice.crm.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Customer response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class ClienteResponse {
    private UUID id;
    private String nombre;
    private String apellido;
    private String rut;
    private String telefono;
    private String email;
    private String direccion;
    private MembresiaActualResponse membresiaActual;
}