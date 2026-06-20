package com.veloservice.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.auth.application.usecase.MecanicoService;
import com.veloservice.auth.interfaces.rest.MecanicoController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MecanicoController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class JwtAuthenticationErrorResponseTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private MecanicoService mecanicoService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void protectedEndpointWithExpiredJwtReturnsExpiredMessage() throws Exception {
        when(jwtTokenProvider.validateTokenStatus("expired.jwt"))
                .thenReturn(JwtTokenProvider.TokenValidationStatus.EXPIRED);

        mockMvc.perform(get("/mecanicos/activos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("JWT expirado"));
    }

    @Test
    void apiRootWithExpiredJwtReturnsUnauthorizedWithExpiredTokenCode() throws Exception {
        when(jwtTokenProvider.validateTokenStatus("expired.jwt"))
                .thenReturn(JwtTokenProvider.TokenValidationStatus.EXPIRED);

        MvcResult result = mockMvc.perform(get("/api/v1/")
                        .contextPath("/api/v1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer expired.jwt"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.path("code").asText(null))
                .as("El mensaje de error no diferencia entre token expirado y ausencia de autenticación")
                .isEqualTo("JWT_EXPIRED");
    }

    @Test
    void protectedEndpointWithInvalidJwtReturnsInvalidMessage() throws Exception {
        when(jwtTokenProvider.validateTokenStatus("invalid.jwt"))
                .thenReturn(JwtTokenProvider.TokenValidationStatus.INVALID);

        mockMvc.perform(get("/mecanicos/activos")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("JWT inválido"));
    }
}
