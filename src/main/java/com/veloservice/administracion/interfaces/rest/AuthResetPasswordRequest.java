package com.veloservice.administracion.interfaces.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Password reset request payload.
 */
@Data
public class AuthResetPasswordRequest {
    @NotBlank
    @Email(regexp = "^[^@]+@[^@]+\\.[^@]+$", message = "Formato de email invalido")
    private String email;
}
