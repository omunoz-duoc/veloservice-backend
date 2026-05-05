package com.veloservice.administracion.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Login response payload containing the JWT and role.
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String rol;
}