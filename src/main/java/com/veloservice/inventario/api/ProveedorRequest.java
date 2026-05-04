package com.veloservice.inventario.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Supplier request payload.
 */
@Data
public class ProveedorRequest {
    @NotBlank
    private String nombre;
    private String rut;
    private String telefono;
    private String email;
    private String direccion;
    private String condicionPago;
    private String contactoAsignado;
}