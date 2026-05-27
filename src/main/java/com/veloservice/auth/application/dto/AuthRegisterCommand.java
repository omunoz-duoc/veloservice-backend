package com.veloservice.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Application command for user registration.
 */
@Data
@AllArgsConstructor
public class AuthRegisterCommand {
    private String nombre;
    private String apellido;
    private String rut;
    private String telefono;
    private String email;
    private String password;
    private UUID sucursalId;
    private String rol;
}
