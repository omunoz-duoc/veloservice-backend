package com.bikeshop.manager.interface_.rest.controller;

import com.bikeshop.manager.application.dto.AuthRequest;
import com.bikeshop.manager.application.dto.AuthResponse;
import com.bikeshop.manager.domain.platform.Rol;
import com.bikeshop.manager.domain.tenant.Usuario;
import com.bikeshop.manager.infrastructure.persistence.repository.RolRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.UsuarioRepository;
import com.bikeshop.manager.infrastructure.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
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
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
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
        usuario.setLastLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        UUID tallerId = usuario.getTaller() != null ? usuario.getTaller().getId() : null;
        String token = jwtProvider.generateToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().getNombre(), tallerId
        );
        return ResponseEntity.ok(new AuthResponse(token, usuario.getRol().getNombre()));
    }

    /**
     * Bootstraps roles and creates the SaaS admin user.
     *
     * @param secret setup secret
     * @param adminEmail admin email
     * @param adminPassword admin password
     * @return setup status
     */
    @PostMapping("/setup")
    public ResponseEntity<String> setup(@RequestParam String secret,
                                        @RequestParam String adminEmail,
                                        @RequestParam String adminPassword) {
        if (!"SETUP_BIKESHOP_2026".equals(secret)) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        if (rolRepository.count() == 0) {
            rolRepository.saveAll(List.of(
                Rol.builder().nombre("ADMIN_SAAS").ambito("plataforma").descripcion("Superadmin").build(),
                Rol.builder().nombre("DUENO").ambito("tenant").descripcion("Dueño taller").build(),
                Rol.builder().nombre("ADMIN").ambito("tenant").descripcion("Administrador").build(),
                Rol.builder().nombre("MECANICO").ambito("tenant").descripcion("Mecánico").build()
            ));
        }
        if (usuarioRepository.existsByEmail(adminEmail)) {
            return ResponseEntity.badRequest().body("Usuario ya existe");
        }
        Rol rolAdmin = rolRepository.findByNombre("ADMIN_SAAS")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Usuario admin = Usuario.builder()
                .rol(rolAdmin).nombre("Admin").apellido("SaaS").rut("1-9")
                .email(adminEmail).telefono("+56900000000")
                .passwordHash(passwordEncoder.encode(adminPassword))
                .activo(true).build();
        usuarioRepository.save(admin);
        return ResponseEntity.ok("Setup completado");
    }
}
