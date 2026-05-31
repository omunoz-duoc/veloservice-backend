package com.veloservice.auth.interfaces.mapper;

import com.veloservice.auth.application.dto.AuthLoginCommand;
import com.veloservice.auth.application.dto.AuthLoginResult;
import com.veloservice.auth.application.dto.AuthRegisterCommand;
import com.veloservice.auth.interfaces.rest.AuthRegisterRequest;
import com.veloservice.auth.interfaces.rest.AuthRequest;
import com.veloservice.auth.interfaces.rest.AuthResponse;

public final class AuthMapper {
    private AuthMapper() {
    }

    public static AuthLoginCommand toCommand(AuthRequest request) {
        return new AuthLoginCommand(request.getEmail(), request.getPassword());
    }

    public static AuthRegisterCommand toCommand(AuthRegisterRequest request) {
        return new AuthRegisterCommand(
                request.getNombre(),
                request.getApellido(),
                request.getRut(),
                request.getTelefono(),
                request.getEmail(),
                request.getPassword(),
                request.getSucursalId(),
                request.getRol()
        );
    }

    public static AuthResponse toResponse(AuthLoginResult result) {
        return new AuthResponse(
                result.getNombre(),
                result.getApellido(),
                result.getToken(),
                result.getRol(),
                result.getAmbito(),
                result.getTallerId(),
                result.getSucursalId()
        );
    }
}
