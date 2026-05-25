package com.veloservice.administracion.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.application.dto.AuthLoginCommand;
import com.veloservice.administracion.application.dto.AuthLoginResult;
import com.veloservice.administracion.application.dto.AuthRegisterCommand;
import com.veloservice.administracion.application.usecase.AuthService;
import com.veloservice.config.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void loginReturnsTokenAndRole() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@veloservice.com");
        request.setPassword("secret");

        AuthLoginResult result = new AuthLoginResult("Nombre", "Apellido","jwt-token", "ADMIN");
        when(authService.login(ArgumentMatchers.any(AuthLoginCommand.class))).thenReturn(result);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.rol").value("ADMIN"));

        verify(authService).login(ArgumentMatchers.any(AuthLoginCommand.class));
    }

    @Test
    void registerReturnsTokenAndRole() throws Exception {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setNombre("Ana");
        request.setApellido("Gomez");
        request.setRut("12.345.678-9");
        request.setTelefono("+56912345678");
        request.setEmail("ana@veloservice.com");
        request.setPassword("secret");
        request.setSucursalId(UUID.fromString("660e8400-e29b-41d4-a716-446655440001"));
        request.setRol("ADMIN_SUCURSAL");

        AuthLoginResult result = new AuthLoginResult("Nombre", "Apellido", "jwt-token", "ADMIN_SUCURSAL");
        when(authService.register(ArgumentMatchers.any(AuthRegisterCommand.class))).thenReturn(result);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.rol").value("ADMIN_SUCURSAL"));

        verify(authService).register(ArgumentMatchers.any(AuthRegisterCommand.class));
    }

    @Test
    void registerWithMalformedJsonReturnsBadRequest() throws Exception {
        String malformedJson = "{\"nombre\":\"Oscar\",}";

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(malformedJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                    .value("JSON invalido. Verifica la sintaxis (comillas, comas y llaves)."));
    }

    @Test
    void loginAdminTallerReturns200WithRole() throws Exception {
        AuthLoginResult result = new AuthLoginResult("Admin", "Taller",
                "eyJhbGciOiJIUzI1NiJ9.fake.sig", "ADMIN_TALLER");
        when(authService.login(ArgumentMatchers.any(AuthLoginCommand.class))).thenReturn(result);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@taller.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN_TALLER"));
    }

    @Test
    void registerWithInvalidRolReturnsBadRequest() throws Exception {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setNombre("Ana");
        request.setApellido("Gomez");
        request.setRut("12.345.678-9");
        request.setTelefono("+56912345678");
        request.setEmail("ana@veloservice.com");
        request.setPassword("secret");
        request.setSucursalId(UUID.fromString("660e8400-e29b-41d4-a716-446655440001"));
        request.setRol("ROLE_NO_EXISTE");

        when(authService.register(ArgumentMatchers.any(AuthRegisterCommand.class)))
                .thenThrow(new IllegalArgumentException("Rol no encontrado"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Rol no encontrado"));
    }
}
