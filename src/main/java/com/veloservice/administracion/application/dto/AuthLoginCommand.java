package com.veloservice.administracion.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application command for user login.
 */
@Data
@AllArgsConstructor
public class AuthLoginCommand {
    private String email;
    private String password;
}
