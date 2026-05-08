package com.veloservice.config.security;

import io.jsonwebtoken.Claims;
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

    /**
     * Creates a provider using configuration properties.
     *
     * @param secret JWT signing secret
     * @param expiration token validity in milliseconds
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration) {
        this.jwtSecret = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpirationMs = expiration;
    }

    /**
     * Generates a signed JWT for the given user.
     *
     * @param userId user identifier
     * @param email user email
     * @param rol role name
     * @param sucursalId branch identifier
     * @return signed JWT
     */
    public String generateToken(UUID userId, String email, String rol, UUID sucursalId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        JwtBuilder builder = Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("rol", rol)
                .claim("sucursalId", sucursalId.toString())
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
        return Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token).getPayload();
    }

    /**
     * Validates the JWT signature and expiration.
     *
     * @param token JWT string
     * @return true if the token is valid
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(jwtSecret).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT invalido: {}", e.getMessage());
            return false;
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

    /**
     * Gets the branch identifier from the JWT.
     *
     * @param token JWT string
     * @return branch identifier
     */
    public UUID getSucursalId(String token) {
        return UUID.fromString(getClaims(token).get("sucursalId", String.class));
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
}