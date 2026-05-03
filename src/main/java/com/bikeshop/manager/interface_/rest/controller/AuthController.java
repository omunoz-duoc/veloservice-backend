package com.bikeshop.manager.interface_.rest.controller;

import com.bikeshop.manager.application.dto.AuthRequest;
import com.bikeshop.manager.application.dto.AuthResponse;
import com.bikeshop.manager.domain.platform.Rol;
import com.bikeshop.manager.domain.platform.Sucursal;
import com.bikeshop.manager.domain.platform.Taller;
import com.bikeshop.manager.domain.tenant.Usuario;
import com.bikeshop.manager.infrastructure.persistence.repository.RolRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.SucursalRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.TallerRepository;
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
    private final TallerRepository tallerRepository;
    private final SucursalRepository sucursalRepository;
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

        UUID tallerId = usuario.getSucursal() != null && usuario.getSucursal().getTaller() != null
            ? usuario.getSucursal().getTaller().getId()
            : null;
        String token = jwtProvider.generateToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().getNombre(), tallerId
        );
        return ResponseEntity.ok(new AuthResponse(token, usuario.getRol().getNombre()));
    }

    /**
    * Bootstraps roles and creates the default workshop owner.
     *
     * @param secret setup secret
    * @param adminEmail owner email
    * @param adminPassword owner password
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
            Rol.builder().nombre("DUENO").descripcion("Dueño taller").build(),
            Rol.builder().nombre("ADMIN").descripcion("Administrador").build(),
            Rol.builder().nombre("MECANICO").descripcion("Mecanico").build()
            ));
        }
        Rol rolDueno = rolRepository.findByNombre("DUENO")
                .orElseThrow(() -> new RuntimeException("Rol DUENO no encontrado"));

        Taller tallerPiloto = tallerRepository.findByRut("76.123.456-7")
                .orElseGet(() -> tallerRepository.save(
                    Taller.builder()
                        .nombre("VeloService")
                        .rut("76.123.456-7")
                .telefono("+56900000000")
                .email("contacto@veloservice.cl")
                .planSaas("base")
                .logoUrl(null)
                        .activo(true)
                        .build()
                ));

        Sucursal sucursalPrincipal = sucursalRepository.findFirstByTaller_Id(tallerPiloto.getId())
            .orElseGet(() -> sucursalRepository.save(
                Sucursal.builder()
                    .taller(tallerPiloto)
                    .nombre("Sede Principal")
                    .direccion("Casa matriz")
                    .telefono("+56900000000")
                    .email("sede@veloservice.cl")
                    .activo(true)
                    .build()
            ));

        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario dueno = Usuario.builder()
                .sucursal(sucursalPrincipal)
                    .rol(rolDueno)
                    .nombre("Carlos")
                    .apellido("Veloso")
                    .rut("15.234.567-8")
                .email(adminEmail)
                    .telefono("+56998765432")
                .passwordHash(passwordEncoder.encode(adminPassword))
                    .activo(true)
                    .build();
            usuarioRepository.save(dueno);
        }

        return ResponseEntity.ok(
            "Setup completado. " +
            "Taller: VeloService | " +
            "Sucursal: Sede Principal | " +
            "Dueno: " + adminEmail
        );
    }
}
