package com.veloservice.administracion.interfaces.mapper;

import com.veloservice.administracion.application.dto.AuthLoginCommand;
import com.veloservice.administracion.application.dto.AuthLoginResult;
import com.veloservice.administracion.interfaces.rest.AuthRequest;
import com.veloservice.administracion.interfaces.rest.AuthResponse;

public final class AuthMapper {
    private AuthMapper() {
    }

    public static AuthLoginCommand toCommand(AuthRequest request) {
        return new AuthLoginCommand(request.getEmail(), request.getPassword());
    }

    public static AuthResponse toResponse(AuthLoginResult result) {
        return new AuthResponse(result.getToken(), result.getRol());
    }
}
