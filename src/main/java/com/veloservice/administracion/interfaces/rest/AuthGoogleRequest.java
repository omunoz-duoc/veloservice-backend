package com.veloservice.administracion.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Google authentication request payload.
 */
@Data
public class AuthGoogleRequest {
    @NotBlank
    private String idToken;
}
