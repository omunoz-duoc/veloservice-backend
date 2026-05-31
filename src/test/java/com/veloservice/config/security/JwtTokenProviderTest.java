package com.veloservice.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private final UUID userId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();
    private final UUID tallerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(
            "test-secret-key-at-least-32-chars!!", 3600000L, 900000L);
    }

    @Test
    void generateTokenWithSucursalAndTallerEmbedsBothClaims() {
        String token = provider.generateToken(userId, "a@b.com", "mecanico", sucursalId, tallerId);
        assertThat(provider.getSucursalId(token)).isEqualTo(sucursalId);
        assertThat(provider.getTallerId(token)).isEqualTo(tallerId);
    }

    @Test
    void generateTokenWithNullSucursalOmitsSucursalClaim() {
        String token = provider.generateToken(userId, "a@b.com", "admin_taller", null, tallerId);
        assertThat(provider.getSucursalId(token)).isNull();
        assertThat(provider.getTallerId(token)).isEqualTo(tallerId);
    }

    @Test
    void getRolReturnsEmbeddedRole() {
        String token = provider.generateToken(userId, "a@b.com", "admin_taller", null, tallerId);
        assertThat(provider.getRol(token)).isEqualTo("admin_taller");
    }
}
