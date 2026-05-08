package com.veloservice.administracion.interfaces.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^\\+?[0-9\\s\\-]{8,20}$", message = "Telefono invalido")
    private String telefono;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
    @NotNull
    private UUID sucursalId;
    @NotBlank
    private String rol;
}
