package com.veloservice.administracion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application command for Google authentication.
 */
@Data
@AllArgsConstructor
public class AuthGoogleCommand {
    private String idToken;
}
