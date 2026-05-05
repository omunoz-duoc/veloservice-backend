package com.veloservice.administracion.interfaces.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login request payload.
 */
@Data
public class AuthRequest {
    @NotBlank
    @Email
    private String email;
    @NotBlank
    private String password;
}