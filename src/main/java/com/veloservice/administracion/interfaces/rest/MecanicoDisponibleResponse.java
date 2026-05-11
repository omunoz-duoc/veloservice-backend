package com.veloservice.administracion.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Available mechanic response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class MecanicoDisponibleResponse {
    private UUID id;
    private String nombre;
    private String apellido;
    private String iniciales;
}
