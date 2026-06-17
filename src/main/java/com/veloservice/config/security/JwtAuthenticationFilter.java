package com.veloservice.config.security;

import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.UsuarioContext;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

/**
 * Extracts JWT credentials and sets the Spring Security context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    public static final String AUTH_ERROR_MESSAGE_ATTRIBUTE =
            JwtAuthenticationFilter.class.getName() + ".AUTH_ERROR_MESSAGE";

    private final JwtTokenProvider tokenProvider;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = null;
        try {
            jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt)) {
                JwtTokenProvider.TokenValidationStatus validationStatus = tokenProvider.validateTokenStatus(jwt);
                if (!validationStatus.isValid()) {
                    request.setAttribute(AUTH_ERROR_MESSAGE_ATTRIBUTE, validationStatus.getResponseMessage());
                    return;
                }

                UUID userId = tokenProvider.getUserId(jwt);
                String rol = tokenProvider.getRol(jwt);
                UUID sucursalId = tokenProvider.getSucursalId(jwt);
                UUID tallerId = tokenProvider.getTallerId(jwt);

                UserDetails userDetails = User.builder()
                        .username(userId.toString())
                        .password("")
                        .authorities(Collections.emptyList())
                        .build();

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, Collections.singletonList(() -> "ROLE_" + rol));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
                if (!"plataforma".equals(tokenProvider.getUserType(jwt))) {
                    UsuarioContext.setCurrentUser(userId);
                }
                if (tallerId != null) {
                    TallerContext.setCurrentTaller(tallerId);
                }
                if (sucursalId != null) {
                    SucursalContext.setCurrentSucursal(sucursalId);
                }
            }
        } catch (ExpiredJwtException e) {
            if (StringUtils.hasText(jwt)) {
                request.setAttribute(
                        AUTH_ERROR_MESSAGE_ATTRIBUTE,
                        JwtTokenProvider.TokenValidationStatus.EXPIRED.getResponseMessage()
                );
            }
            log.error("Error procesando JWT", e);
        } catch (Exception e) {
            if (StringUtils.hasText(jwt)) {
                request.setAttribute(
                        AUTH_ERROR_MESSAGE_ATTRIBUTE,
                        JwtTokenProvider.TokenValidationStatus.INVALID.getResponseMessage()
                );
            }
            log.error("Error procesando JWT", e);
        } finally {
            try {
                filterChain.doFilter(request, response);
            } finally {
                TallerContext.clear();
                SucursalContext.clear();
                UsuarioContext.clear();
            }
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
