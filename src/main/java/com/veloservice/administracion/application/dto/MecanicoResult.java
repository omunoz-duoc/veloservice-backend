package com.veloservice.administracion.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Result for mechanic listings.
 */
@Value
@Builder
public class MecanicoResult {
    UUID id;
    String nombre;
    String apellido;
    String iniciales;
    String email;
    Boolean activo;
    UUID sucursalId;
}
