package com.veloservice.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * Application result for user login.
 */
@Data
@AllArgsConstructor
public class AuthLoginResult {
    private String nombre;
    private String apellido;
    private String token;
    private String rol;
    private String ambito;
    private UUID tallerId;
    private UUID sucursalId;
}
