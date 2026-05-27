package com.veloservice.auth.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Change password request payload.
 */
@Data
public class AuthChangePasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
