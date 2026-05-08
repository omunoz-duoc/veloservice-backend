package com.veloservice.administracion.application.usecase;

import com.veloservice.administracion.application.dto.AuthLoginCommand;
import com.veloservice.administracion.application.dto.AuthLoginResult;
import com.veloservice.administracion.application.dto.AuthRegisterCommand;
import com.veloservice.administracion.domain.model.Rol;
import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.RolRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * Registers a new user and returns a JWT token.
     *
     * @param command registration command payload
     * @return authentication response
     */
    @Transactional
    public AuthLoginResult register(AuthRegisterCommand command) {
        if (usuarioRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Email ya registrado");
        }

        Rol rol = rolRepository.findByNombre(command.getRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(command.getSucursalId())
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada"));

        Usuario usuario = Usuario.builder()
                .nombre(command.getNombre())
                .apellido(command.getApellido())
                .rut(command.getRut())
                .telefono(command.getTelefono())
                .email(command.getEmail())
                .passwordHash(passwordEncoder.encode(command.getPassword()))
                .rol(rol)
                .sucursal(sucursal)
                .build();

        Usuario saved = usuarioRepository.save(usuario);
        String token = jwtProvider.generateToken(
                saved.getId(), saved.getEmail(), rol.getNombre(), sucursal.getId()
        );
        return new AuthLoginResult(token, rol.getNombre());
    }
}
