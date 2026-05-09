package com.veloservice.administracion.application.usecase;

import com.veloservice.administracion.application.dto.AuthLoginCommand;
import com.veloservice.administracion.application.dto.AuthLoginResult;
import com.veloservice.administracion.application.dto.AuthRegisterCommand;
import com.veloservice.administracion.domain.model.PasswordResetToken;
import com.veloservice.administracion.domain.model.Rol;
import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.email.ResendEmailService;
import com.veloservice.administracion.infraestructure.persistence.repository.PasswordResetTokenRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.RolRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.administracion.infraestructure.ratelimit.PasswordResetRateLimiter;
import com.veloservice.config.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Handles authentication and token issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarioRepository;
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final PasswordResetRateLimiter passwordResetRateLimiter;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
        private final ResendEmailService resendEmailService;

        @Value("${jwt.reset-expiration:900000}")
        private long resetExpirationMs;

        private static final int RESET_TOKEN_BYTES = 32;
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

    /**
     * Resets the user's password.
     *
     * @param email the user's email
         * @param clientIp client IP address
     */
        @Transactional
        public void resetPassword(String email, String clientIp) {
                if (!passwordResetRateLimiter.allow(email, clientIp)) {
                        return;
                }

                Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(email).orElse(null);
                if (usuario == null) {
                        return;
                }

                passwordResetTokenRepository.deleteByUsuarioId(usuario.getId());

                String rawToken = generateResetToken();
                String tokenHash = hashToken(rawToken);
                OffsetDateTime expiresAt = OffsetDateTime.now().plus(Duration.ofMillis(resetExpirationMs));

                PasswordResetToken resetToken = PasswordResetToken.builder()
                                .usuario(usuario)
                                .tokenHash(tokenHash)
                                .expiresAt(expiresAt)
                                .build();
                passwordResetTokenRepository.save(resetToken);

                resendEmailService.sendPasswordResetEmail(usuario.getEmail(), usuario.getNombre(), rawToken);
    }

        /**
         * Changes the user's password using a reset token.
         *
         * @param token reset token
         * @param newPassword new password
         */
    @Transactional
    public void changePassword(String token, String newPassword) {
        String tokenHash = hashToken(token);
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedFalseAndExpiresAtAfter(tokenHash, OffsetDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("TOKEN_INVALID"));

        Usuario usuario = resetToken.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private String generateResetToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
