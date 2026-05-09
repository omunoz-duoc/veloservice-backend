package com.veloservice.administracion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

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
}
