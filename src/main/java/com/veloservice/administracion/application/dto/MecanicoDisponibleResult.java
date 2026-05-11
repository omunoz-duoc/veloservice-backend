package com.veloservice.administracion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Application result for available mechanics.
 */
@Data
@Builder
@AllArgsConstructor
public class MecanicoDisponibleResult {
    private UUID id;
    private String nombre;
    private String apellido;
    private String iniciales;
}
