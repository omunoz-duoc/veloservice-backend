package com.veloservice.administracion.application.usecase;

import com.veloservice.administracion.application.dto.AuthLoginCommand;
import com.veloservice.administracion.application.dto.AuthLoginResult;
import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Handles authentication and token issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtProvider;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request login request payload
     * @return authentication response
     */
    public AuthLoginResult login(AuthLoginCommand command) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.getEmail(), command.getPassword())
        );
        Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(command.getEmail()).orElseThrow();
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
        return new AuthLoginResult(token, usuario.getRol().getNombre());
    }
}
