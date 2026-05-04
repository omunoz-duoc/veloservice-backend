package com.veloservice.administracion.web;

import com.veloservice.administracion.api.AuthRequest;
import com.veloservice.administracion.api.AuthResponse;
import com.veloservice.administracion.internal.entity.Usuario;
import com.veloservice.administracion.internal.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Authentication endpoints for login and initial setup.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtProvider;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request login request payload
     * @return authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(request.getEmail()).orElseThrow();
        usuario.setLastLogin(OffsetDateTime.now());
        usuarioRepository.save(usuario);

        UUID sucursalId = usuario.getSucursal() != null
            ? usuario.getSucursal().getId()
            : null;
        if (sucursalId == null) {
            throw new IllegalStateException("Usuario sin sucursal asignada");
        }
        String token = jwtProvider.generateToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().getNombre(), sucursalId
        );
        return ResponseEntity.ok(new AuthResponse(token, usuario.getRol().getNombre()));
    }
}