package com.veloservice.auth.interfaces.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

/**
 * Registration request payload.
 */
@Data
public class AuthRegisterRequest {
    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @NotBlank
    private String rut;

    @NotBlank
    private String telefono;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private UUID sucursalId;

    @NotBlank
    private String rol;
}