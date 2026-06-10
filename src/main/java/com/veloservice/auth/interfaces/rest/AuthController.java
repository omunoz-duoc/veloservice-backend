package com.veloservice.auth.interfaces.rest;

import com.veloservice.auth.application.dto.AuthLoginResult;
import com.veloservice.auth.application.usecase.AuthService;
import com.veloservice.auth.interfaces.mapper.AuthMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }

    /**
     * Registers a user and returns a JWT token.
     *
     * @param request registration request payload
     * @return authentication response
     */
    @PostMapping("/login_admin")
    public ResponseEntity<AuthResponse> loginAdmin(@Valid @RequestBody AuthRequest request) {
        AuthLoginResult result = authService.loginPlataforma(AuthMapper.toCommand(request));
        return ResponseEntity.ok(AuthMapper.toResponse(result));
    }

    @PreAuthorize("hasRole('admin_taller') or hasRole('plataforma')")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        AuthLoginResult result = authService.register(AuthMapper.toCommand(request));
        return ResponseEntity.ok(AuthMapper.toResponse(result));
    }

    /**
     * Sends a password reset email to the user.
     * 
     * @param request password reset request payload
     * @return success response
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(
            @Valid @RequestBody AuthResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        boolean allowed = authService.resetPassword(request.getEmail(), resolveClientIp(httpRequest));
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        return ResponseEntity.ok().build();        
    }

    /**
     * Changes the user's password using a reset token.
     *
     * @param request change password request payload
     * @return success response
     */
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody AuthChangePasswordRequest request) {
        authService.changePassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.noContent().build(); 
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
    
}
