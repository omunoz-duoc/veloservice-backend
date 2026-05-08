package com.veloservice.administracion.interfaces.rest;

import com.veloservice.administracion.application.dto.AuthLoginResult;
import com.veloservice.administracion.application.usecase.AuthService;
import com.veloservice.administracion.interfaces.mapper.AuthMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints for login and initial setup.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request login request payload
     * @return authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthLoginResult result = authService.login(AuthMapper.toCommand(request));
        return ResponseEntity.ok(AuthMapper.toResponse(result));
    }

    /**
     * Registers a user and returns a JWT token.
     *
     * @param request registration request payload
     * @return authentication response
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        AuthLoginResult result = authService.register(AuthMapper.toCommand(request));
        return ResponseEntity.ok(AuthMapper.toResponse(result));
    }
}