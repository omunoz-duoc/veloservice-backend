package com.bikeshop.manager.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Customer creation request payload.
 */
@Data
public class ClienteRequest {
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;
    @NotBlank
    private String rut;
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9\\s\\-]{8,20}$", message = "Teléfono inválido")
    private String telefono;
    private String email;
    private String direccion;
}
