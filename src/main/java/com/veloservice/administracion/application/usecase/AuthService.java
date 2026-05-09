package com.veloservice.administracion.application.usecase;

import com.veloservice.administracion.application.dto.AuthLoginCommand;
import com.veloservice.administracion.application.dto.AuthLoginResult;
import com.veloservice.administracion.application.dto.AuthRegisterCommand;
import com.veloservice.administracion.application.exception.AuthErrorCode;
import com.veloservice.administracion.application.exception.AuthException;
import com.veloservice.administracion.application.security.LoginAttemptService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

/**
 * Handles authentication and token issuance.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarioRepository;
        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final PasswordResetRateLimiter passwordResetRateLimiter;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
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
        String email = command.getEmail();
        if (loginAttemptService.isBlocked(email)) {
            throw new AuthException(AuthErrorCode.TOO_MANY_ATTEMPTS);
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new AuthException(AuthErrorCode.ACCOUNT_NOT_VERIFIED);
        }
        if (!passwordEncoder.matches(command.getPassword(), usuario.getPasswordHash())) {
            loginAttemptService.recordFailedAttempt(email);
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }
        loginAttemptService.resetAttempts(email);

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
        return new AuthLoginResult(usuario.getNombre(), usuario.getApellido(), token, usuario.getRol().getNombre());
    }

    /**
     * Registers a new user and returns a JWT token.
     *
     * @param command registration command payload
     * @return authentication response
     */
    @Transactional
    public AuthLoginResult register(AuthRegisterCommand command) {
        Sucursal sucursal = validateSucursal(command.getSucursalId());
        Rol rol = validateRol(command.getRol());
        validatePassword(command.getPassword());
        validateEmail(command.getEmail());
        validateRut(command.getRut());
        validateTelefono(command.getTelefono());
        validateApellido(command.getApellido());
        validateNombre(command.getNombre());

        validateEmailUnique(command.getEmail());

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
        return new AuthLoginResult(usuario.getNombre(), usuario.getApellido(), token, rol.getNombre());
    }

    /**
     * Resets the user's password.
     *
     * @param email the user's email
         * @param clientIp client IP address
     * @return true when request is allowed
     */
        @Transactional
    public boolean resetPassword(String email, String clientIp) {
                if (!passwordResetRateLimiter.allow(email, clientIp)) {
            return false;
                }

                Usuario usuario = usuarioRepository.findByEmailAndActivoTrue(email).orElse(null);
                if (usuario == null) {
            return true;
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
                return true;
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

    private Sucursal validateSucursal(UUID sucursalId) {
        if (sucursalId == null) {
            throw new IllegalArgumentException("INVALID_SUCURSAL");
        }
        return sucursalRepository.findById(sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_SUCURSAL"));
    }

    private Rol validateRol(String rolNombre) {
        if (!StringUtils.hasText(rolNombre)) {
            throw new IllegalArgumentException("INVALID_ROL");
        }
        return rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new IllegalArgumentException("INVALID_ROL"));
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("INVALID_PASSWORD");
        }
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSymbol = true;
            }
        }
        if (!(hasUpper && hasDigit && hasSymbol)) {
            throw new IllegalArgumentException("INVALID_PASSWORD");
        }
    }

    private void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("INVALID_EMAIL");
        }
        String trimmed = email.trim();
        if (!trimmed.matches("^[^@]+@[^@]+\\.[^@]+$")) {
            throw new IllegalArgumentException("INVALID_EMAIL");
        }
    }

    private void validateEmailUnique(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("EMAIL_ALREADY_EXISTS");
        }
    }

    private void validateRut(String rut) {
        if (!isValidRut(rut)) {
            throw new IllegalArgumentException("INVALID_RUT");
        }
    }

    private void validateTelefono(String telefono) {
        if (!StringUtils.hasText(telefono) || !telefono.matches("^\\d{9}$")) {
            throw new IllegalArgumentException("INVALID_TELEFONO");
        }
    }

    private void validateApellido(String apellido) {
        if (!isValidNombre(apellido)) {
            throw new IllegalArgumentException("INVALID_APELLIDO");
        }
    }

    private void validateNombre(String nombre) {
        if (!isValidNombre(nombre)) {
            throw new IllegalArgumentException("INVALID_NOMBRE");
        }
    }

    private boolean isValidNombre(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.length() <= 50 && trimmed.matches("^\\p{L}+$");
    }

    private boolean isValidRut(String rut) {
        if (!StringUtils.hasText(rut)) {
            return false;
        }
        String cleaned = rut.replace(".", "")
                .replace("-", "")
                .replace(" ", "")
                .toUpperCase(Locale.ROOT);
        if (cleaned.length() < 2) {
            return false;
        }

        String body = cleaned.substring(0, cleaned.length() - 1);
        String dv = cleaned.substring(cleaned.length() - 1);
        if (!body.matches("^\\d+$") || !dv.matches("^[0-9K]$")) {
            return false;
        }

        int sum = 0;
        int multiplier = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(body.charAt(i));
            sum += digit * multiplier;
            multiplier = (multiplier == 7) ? 2 : multiplier + 1;
        }
        int remainder = 11 - (sum % 11);
        String expected;
        if (remainder == 11) {
            expected = "0";
        } else if (remainder == 10) {
            expected = "K";
        } else {
            expected = String.valueOf(remainder);
        }
        return expected.equals(dv);
    }
}
