package com.veloservice.auth.interfaces.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthMeChangePasswordRequest {
    @NotBlank
    private String actual;

    @NotBlank
    private String nueva;
}
