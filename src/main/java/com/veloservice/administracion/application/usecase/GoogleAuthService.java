package com.veloservice.administracion.application.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.application.dto.AuthGoogleCommand;
import com.veloservice.administracion.application.dto.AuthLoginResult;
import com.veloservice.administracion.application.exception.GoogleAuthErrorCode;
import com.veloservice.administracion.application.exception.GoogleAuthException;
import com.veloservice.administracion.domain.model.Rol;
import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.RolRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * Handles Google OAuth2 login and local user provisioning.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RANDOM_PASSWORD_BYTES = 32;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final JwtTokenProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Transactional
    public AuthLoginResult loginGoogle(AuthGoogleCommand command) {
        GoogleTokenInfo tokenInfo = validateAndReadToken(command.getIdToken());
        String email = normalizeEmail(tokenInfo.email());

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(tokenInfo, email));

        log.info("Google login para {}", email);

        UUID sucursalId = usuario.getSucursal() != null ? usuario.getSucursal().getId() : null;
        if (sucursalId == null) {
            throw new GoogleAuthException(GoogleAuthErrorCode.SUCURSAL_NOT_FOUND);
        }

        String token = jwtProvider.generateToken(
                usuario.getId(), usuario.getEmail(), usuario.getRol().getNombre(), sucursalId
        );
        return new AuthLoginResult(usuario.getNombre(), usuario.getApellido(), token, usuario.getRol().getNombre());
    }

    private GoogleTokenInfo validateAndReadToken(String idToken) {
        if (!StringUtils.hasText(idToken)) {
            throw new GoogleAuthException(GoogleAuthErrorCode.INVALID_TOKEN);
        }

        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new GoogleAuthException(GoogleAuthErrorCode.INVALID_TOKEN);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String email = text(root, "email");
            Boolean emailVerified = booleanValue(root, "email_verified");
            if (!StringUtils.hasText(email) || !Boolean.TRUE.equals(emailVerified)) {
                throw new GoogleAuthException(GoogleAuthErrorCode.EMAIL_NOT_FOUND);
            }

            return new GoogleTokenInfo(
                    email,
                    text(root, "given_name"),
                    text(root, "family_name"),
                    text(root, "name")
            );
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new GoogleAuthException(GoogleAuthErrorCode.INVALID_TOKEN);
        } catch (IOException ex) {
            throw new GoogleAuthException(GoogleAuthErrorCode.INVALID_TOKEN);
        }
    }

    @SuppressWarnings("null")
    private Usuario createGoogleUser(GoogleTokenInfo tokenInfo, String email) {
        Rol rol = findOrCreateClienteRole();
        Sucursal sucursal = resolveDefaultSucursal();
        String nombre = firstText(tokenInfo.givenName(), tokenInfo.fullName(), email);
        String apellido = firstText(tokenInfo.familyName(), tokenInfo.fullName(), email);

        Usuario usuario = Usuario.builder()
                .nombre(nombre)
                .apellido(apellido)
                .email(email)
                .passwordHash(passwordEncoder.encode(generateRandomPassword()))
                .rol(rol)
                .sucursal(sucursal)
                .activo(true)
                .build();

        return usuarioRepository.save(usuario);
    }

    @SuppressWarnings("null")
    private Rol findOrCreateClienteRole() {
        return rolRepository.findByNombre("CLIENTE")
                .orElseGet(() -> rolRepository.save(Rol.builder()
                        .nombre("CLIENTE")
                        .descripcion("Cliente")
                        .activo(true)
                        .build()));
    }

    private Sucursal resolveDefaultSucursal() {
        return sucursalRepository.findFirstByActivoTrueOrderByCreatedAtAsc()
                .orElseThrow(() -> new GoogleAuthException(GoogleAuthErrorCode.SUCURSAL_NOT_FOUND));
    }

    private String generateRandomPassword() {
        byte[] bytes = new byte[RANDOM_PASSWORD_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new GoogleAuthException(GoogleAuthErrorCode.EMAIL_NOT_FOUND);
        }
        return email.trim().toLowerCase();
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asText(null) : null;
    }

    private Boolean booleanValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && !value.isNull() ? value.asBoolean() : null;
    }

    private String firstText(String primary, String secondary, String fallback) {
        if (StringUtils.hasText(primary)) {
            return primary.trim();
        }
        if (StringUtils.hasText(secondary)) {
            return secondary.trim();
        }
        return fallback;
    }

    private record GoogleTokenInfo(String email, String givenName, String familyName, String fullName) {
    }
}
