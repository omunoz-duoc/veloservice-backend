package com.veloservice.config.security;

import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.UsuarioContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final UUID userId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();
    private final UUID tallerId = UUID.randomUUID();

    @AfterEach
    void cleanup() {
        SucursalContext.clear();
        TallerContext.clear();
        UsuarioContext.clear();
    }

    @Test
    void tallerOnlyTokenSetsTallerAndUserContextsWithoutSucursal() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader("Authorization")).willReturn("Bearer valid.token.here");
        given(tokenProvider.validateToken("valid.token.here")).willReturn(true);
        given(tokenProvider.getUserId("valid.token.here")).willReturn(userId);
        given(tokenProvider.getRol("valid.token.here")).willReturn("admin_taller");
        given(tokenProvider.getSucursalId("valid.token.here")).willReturn(null);
        given(tokenProvider.getTallerId("valid.token.here")).willReturn(tallerId);
        given(tokenProvider.getUserType("valid.token.here")).willReturn(null);

        UUID[] capturedTaller = new UUID[1];
        UUID[] capturedSucursal = new UUID[1];
        UUID[] capturedUser = new UUID[1];
        doAnswer(invocation -> {
            capturedTaller[0] = TallerContext.getCurrentTaller();
            capturedSucursal[0] = SucursalContext.getCurrentSucursal();
            capturedUser[0] = UsuarioContext.getCurrentUser();
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedTaller[0]).isEqualTo(tallerId);
        assertThat(capturedSucursal[0]).isNull();
        assertThat(capturedUser[0]).isEqualTo(userId);
    }

    @Test
    void sucursalTokenSetsTallerSucursalAndUserContexts() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        given(request.getHeader("Authorization")).willReturn("Bearer valid.token.here");
        given(tokenProvider.validateToken("valid.token.here")).willReturn(true);
        given(tokenProvider.getUserId("valid.token.here")).willReturn(userId);
        given(tokenProvider.getRol("valid.token.here")).willReturn("mecanico");
        given(tokenProvider.getSucursalId("valid.token.here")).willReturn(sucursalId);
        given(tokenProvider.getTallerId("valid.token.here")).willReturn(tallerId);
        given(tokenProvider.getUserType("valid.token.here")).willReturn(null);

        UUID[] capturedTaller = new UUID[1];
        UUID[] capturedSucursal = new UUID[1];
        UUID[] capturedUser = new UUID[1];
        doAnswer(invocation -> {
            capturedTaller[0] = TallerContext.getCurrentTaller();
            capturedSucursal[0] = SucursalContext.getCurrentSucursal();
            capturedUser[0] = UsuarioContext.getCurrentUser();
            return null;
        }).when(chain).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        assertThat(capturedTaller[0]).isEqualTo(tallerId);
        assertThat(capturedSucursal[0]).isEqualTo(sucursalId);
        assertThat(capturedUser[0]).isEqualTo(userId);
    }
}
