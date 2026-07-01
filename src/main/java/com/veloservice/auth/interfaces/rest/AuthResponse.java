package com.veloservice.auth.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Login response payload containing the JWT and role.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String nombre;
    private String apellido;
    private String token;
    private String rol;
    private String ambito;
    private UUID tallerId;
    private UUID sucursalId;
}
