package com.veloservice.auth.application.usecase;

import com.veloservice.administracion.domain.model.UsuarioSucursal;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioSucursalRepository;
import com.veloservice.auth.application.dto.AuthLoginCommand;
import com.veloservice.auth.application.port.SucursalPort;
import com.veloservice.auth.application.dto.AuthLoginResult;
import com.veloservice.auth.application.dto.AuthRegisterCommand;
import com.veloservice.auth.application.exception.AuthErrorCode;
import com.veloservice.auth.application.exception.AuthException;
import com.veloservice.auth.application.security.LoginAttemptService;
import com.veloservice.auth.domain.model.PasswordResetToken;
import com.veloservice.auth.domain.model.Rol;
import com.veloservice.auth.domain.model.Usuario;
import com.veloservice.auth.infraestructure.email.ResendEmailService;
import com.veloservice.auth.infraestructure.persistence.repository.PasswordResetTokenRepository;
import com.veloservice.auth.infraestructure.persistence.repository.RolRepository;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioPlataformaRepository;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.auth.infraestructure.ratelimit.PasswordResetRateLimiter;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.shared.application.exception.ResourceNotFoundException;
import com.veloservice.shared.application.util.RutUtils;
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
    private final UsuarioPlataformaRepository usuarioPlataformaRepository;
    private final UsuarioSucursalRepository usuarioSucursalRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetRateLimiter passwordResetRateLimiter;
    private final RolRepository rolRepository;
    private final SucursalPort sucursalPort;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptService loginAttemptService;
    private final ResendEmailService resendEmailService;

    @Value("${jwt.reset-expiration:900000}")
    private long resetExpirationMs;

    private static final int RESET_TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public boolean rutExists(String rut) {
        if (!StringUtils.hasText(rut)) {
            return false;
        }
        return usuarioRepository.existsByNormalizedRut(RutUtils.normalize(rut));
    }

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

        Scope scope = resolveScope(usuario);

        String token = jwtProvider.generateToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().getNombre(), scope.sucursalId(), scope.tallerId()
        );
        return new AuthLoginResult(
                usuario.getNombre(),
                usuario.getApellido(),
                token,
                usuario.getRol().getNombre(),
                scope.ambito(),
                scope.tallerId(),
                scope.sucursalId()
        );
    }

    @Transactional
    public AuthLoginResult loginPlataforma(AuthLoginCommand command) {
        String email = command.getEmail();
        if (loginAttemptService.isBlocked(email)) {
            throw new AuthException(AuthErrorCode.TOO_MANY_ATTEMPTS);
        }

        com.veloservice.auth.domain.model.UsuarioPlataforma usuario =
                usuarioPlataformaRepository.findByEmailAndActivoTrue(email)
                        .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(command.getPassword(), usuario.getPasswordHash())) {
            loginAttemptService.recordFailedAttempt(email);
            throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
        }
        loginAttemptService.resetAttempts(email);

        usuario.setLastLogin(OffsetDateTime.now());
        usuarioPlataformaRepository.save(usuario);

        String token = jwtProvider.generatePlatformToken(usuario.getId(), usuario.getEmail());
        return new AuthLoginResult(usuario.getNombre(), usuario.getApellido(), token, "plataforma", "plataforma", null, null);
    }

    /**
     * Registers a new user and returns a JWT token.
     *
     * @param command registration command payload
     * @return authentication response
     */
    @Transactional
    public AuthLoginResult register(AuthRegisterCommand command) {
        SucursalPort.SucursalRef sucursal = validateSucursal(command.getSucursalId());
        Rol rol = validateRol(command.getRol());
        validatePassword(command.getPassword());
        validateEmail(command.getEmail());
        validateRut(command.getRut());
        validateTelefono(command.getTelefono());
        validateApellido(command.getApellido());
        validateNombre(command.getNombre());

        validateEmailUnique(command.getEmail());
        UUID tallerId = sucursalPort.findTallerIdBySucursalId(sucursal.id())
                .orElseThrow(() -> new IllegalArgumentException("INVALID_SUCURSAL"));

        Usuario usuario = Usuario.builder()
                .tallerId(tallerId)
                .rolId(rol.getId())
                .nombre(command.getNombre())
                .apellido(command.getApellido())
                .rut(command.getRut())
                .telefono(command.getTelefono())
                .email(command.getEmail())
                .passwordHash(passwordEncoder.encode(command.getPassword()))
                .rol(rol)
                .sucursalId(sucursal.id())
                .build();

        Usuario saved = usuarioRepository.save(usuario);

        UUID tokenSucursalId = "taller".equals(normalizeAmbito(rol.getAmbito())) ? null : sucursal.id();

        String token = jwtProvider.generateToken(
                saved.getId(), saved.getEmail(), rol.getNombre(), tokenSucursalId, tallerId
        );
        return new AuthLoginResult(
                usuario.getNombre(),
                usuario.getApellido(),
                token,
                rol.getNombre(),
                normalizeAmbito(rol.getAmbito()),
                tallerId,
                tokenSucursalId
        );
    }

    private Scope resolveScope(Usuario usuario) {
        String ambito = normalizeAmbito(usuario.getRol().getAmbito());
        UUID tallerId = usuario.getTallerId();
        if ("taller".equals(ambito)) {
            UUID sucursalId = usuarioSucursalRepository.findByUsuarioIdAndEsPrincipalTrue(usuario.getId())
                    .map(UsuarioSucursal::getSucursalId)
                    .map(sid -> validarSucursalPrincipalDelTaller(sid, tallerId))
                    .orElse(null);
            return new Scope(ambito, tallerId, sucursalId);
        }
        if (!"sucursal".equals(ambito)) {
            throw new AuthException(AuthErrorCode.AMBITO_ROL_INVALIDO);
        }

        UsuarioSucursal principal = usuarioSucursalRepository.findByUsuarioIdAndEsPrincipalTrue(usuario.getId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USUARIO_SIN_SUCURSAL_PRINCIPAL));
        UUID sucursalTallerId = sucursalPort.findTallerIdBySucursalId(principal.getSucursalId())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USUARIO_SIN_SUCURSAL_PRINCIPAL));
        if (!sucursalTallerId.equals(tallerId)) {
            throw new AuthException(AuthErrorCode.SUCURSAL_NO_PERTENECE_TALLER);
        }

        return new Scope(ambito, tallerId, principal.getSucursalId());
    }

    private UUID validarSucursalPrincipalDelTaller(UUID sucursalId, UUID tallerId) {
        UUID sucursalTallerId = sucursalPort.findTallerIdBySucursalId(sucursalId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USUARIO_SIN_SUCURSAL_PRINCIPAL));
        if (!sucursalTallerId.equals(tallerId)) {
            throw new AuthException(AuthErrorCode.SUCURSAL_NO_PERTENECE_TALLER);
        }
        return sucursalId;
    }

    private String normalizeAmbito(String ambito) {
        return StringUtils.hasText(ambito) ? ambito.trim().toLowerCase(Locale.ROOT) : "";
    }

    private record Scope(String ambito, UUID tallerId, UUID sucursalId) {
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
            return false;
        }

        passwordResetTokenRepository.deleteByUsuarioId(usuario.getId());

        String rawToken = generateResetToken();
        String tokenHash = hashToken(rawToken);
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expiresAt = now.plus(Duration.ofMillis(resetExpirationMs));

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .userId(usuario.getId())
                .usuario(usuario)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .used(false)
                .createdAt(now)
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
        validatePassword(newPassword);
        String tokenHash = hashToken(token);
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedFalseAndExpiresAtAfter(tokenHash, OffsetDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));

        Usuario usuario = resetToken.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Transactional
    public void changeCurrentUserPassword(String actual, String nueva) {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) {
            throw new ResourceNotFoundException("Usuario autenticado no encontrado");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));

        if (!passwordEncoder.matches(actual, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Contrasena actual incorrecta");
        }

        validatePassword(nueva);
        usuario.setPasswordHash(passwordEncoder.encode(nueva));
        usuario.setUpdatedAt(OffsetDateTime.now());
        usuarioRepository.save(usuario);
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

    private SucursalPort.SucursalRef validateSucursal(UUID sucursalId) {
        if (sucursalId == null) {
            throw new IllegalArgumentException("INVALID_SUCURSAL");
        }
        return sucursalPort.findById(sucursalId)
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
        if (!StringUtils.hasText(telefono) || !telefono.matches("^\\+569\\d{8,11}$")) {
            throw new IllegalArgumentException("INVALID_PHONE");
        }
    }

    private void validateApellido(String apellido) {
        if (!isValidNombre(apellido)) {
            throw new IllegalArgumentException("INVALID_SURNAME");
        }
    }

    private void validateNombre(String nombre) {
        if (!isValidNombre(nombre)) {
            throw new IllegalArgumentException("INVALID_NAME");
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
        String cleaned = RutUtils.normalize(rut);
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
