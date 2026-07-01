package com.veloservice.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Generates and validates JWT tokens.
 */
@Component
@Slf4j
public class JwtTokenProvider {
    private final SecretKey jwtSecret;
    private final long jwtExpirationMs;
    private final long resetExpirationMs;

    public enum TokenValidationStatus {
        VALID(null),
        EXPIRED("JWT expirado"),
        INVALID("JWT inválido");

        private final String responseMessage;

        TokenValidationStatus(String responseMessage) {
            this.responseMessage = responseMessage;
        }

        public boolean isValid() {
            return this == VALID;
        }

        public String getResponseMessage() {
            return responseMessage;
        }
    }

    /**
     * Creates a provider using configuration properties.
     *
     * @param secret JWT signing secret
     * @param expiration token validity in milliseconds
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration,
                            @Value("${jwt.reset-expiration:900000}") long resetExpirationMs) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = expiration;
        this.resetExpirationMs = resetExpirationMs;
    }

    /**
     * Generates a signed JWT for the given user.
     *
     * @param userId user identifier
     * @param email user email
     * @param rol role name
     * @param sucursalId optional branch identifier; when non-null, embedded as "sucursalId" claim
     * @param tallerId optional tenant identifier; when non-null, embedded as "tallerId" claim
     * @return signed JWT
     */
    public String generatePlatformToken(UUID userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("rol", "plataforma")
                .claim("userType", "plataforma")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(jwtSecret, Jwts.SIG.HS256)
                .compact();
    }

    public String generateToken(UUID userId, String email, String rol, UUID sucursalId, UUID tallerId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("rol", rol)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(jwtSecret, Jwts.SIG.HS256);
        if (sucursalId != null) {
            builder.claim("sucursalId", sucursalId.toString());
        }
        if (tallerId != null) {
            builder.claim("tallerId", tallerId.toString());
        }
        return builder.compact();
    }

    /**
     * Generates a signed JWT for password reset.
     *
     * @param userId user identifier
     * @param email user email
     * @return signed reset JWT
     */
    public String generatePasswordResetToken(UUID userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + resetExpirationMs);
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("type", "password_reset")
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(jwtSecret, Jwts.SIG.HS256);
        return builder.compact();
    }

    /**
     * Extracts token claims.
     *
     * @param token JWT string
     * @return token claims
     */
    public Claims getClaims(String token) {
        return parseClaims(token);
    }

    /**
     * Validates the JWT signature and expiration.
     *
     * @param token JWT string
     * @return true if the token is valid
     */
    public boolean validateToken(String token) {
        return validateTokenStatus(token).isValid();
    }

    public TokenValidationStatus validateTokenStatus(String token) {
        try {
            parseClaims(token);
            return TokenValidationStatus.VALID;
        } catch (ExpiredJwtException e) {
            log.error("JWT expirado: {}", e.getMessage());
            return TokenValidationStatus.EXPIRED;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT invalido: {}", e.getMessage());
            return TokenValidationStatus.INVALID;
        }
    }

    /**
     * Gets the user identifier from the JWT.
     *
     * @param token JWT string
     * @return user identifier
     */
    public UUID getUserId(String token) {
        return UUID.fromString(getClaims(token).get("userId", String.class));
    }

    /**
     * Gets the role name from the JWT.
     *
     * @param token JWT string
     * @return role name
     */
    public String getRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    public String getUserType(String token) {
        return getClaims(token).get("userType", String.class);
    }

    /**
     * Gets the branch identifier from the JWT.
     *
     * @param token JWT string
     * @return branch identifier
     */
    public UUID getSucursalId(String token) {
        String sid = getClaims(token).get("sucursalId", String.class);
        return sid != null ? UUID.fromString(sid) : null;
    }

    /**
     * Gets the tenant identifier from the JWT.
     *
     * @param token JWT string
     * @return tenant identifier or null
     */
    public UUID getTallerId(String token) {
        String tid = getClaims(token).get("tallerId", String.class);
        return tid != null ? UUID.fromString(tid) : null;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
    }
}
