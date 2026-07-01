package com.veloservice.administracion.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.auth.application.dto.AuthLoginCommand;
import com.veloservice.auth.application.dto.AuthLoginResult;
import com.veloservice.auth.application.dto.AuthRegisterCommand;
import com.veloservice.auth.application.usecase.AuthService;
import com.veloservice.auth.interfaces.rest.AuthController;
import com.veloservice.auth.interfaces.rest.AuthRegisterRequest;
import com.veloservice.auth.interfaces.rest.AuthRequest;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.shared.application.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    private static final UUID TALLER_ID = UUID.randomUUID();
    private static final UUID SUCURSAL_ID = UUID.randomUUID();

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

        AuthLoginResult result = new AuthLoginResult(
                "Nombre", "Apellido", "jwt-token", "admin_taller", "taller", TALLER_ID, null);
        when(authService.login(ArgumentMatchers.any(AuthLoginCommand.class))).thenReturn(result);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.rol").value("admin_taller"))
            .andExpect(jsonPath("$.ambito").value("taller"))
            .andExpect(jsonPath("$.tallerId").value(TALLER_ID.toString()))
            .andExpect(jsonPath("$.sucursalId").doesNotExist());

        verify(authService).login(ArgumentMatchers.any(AuthLoginCommand.class));
    }

    @Test
    void logoutReturnsNoContent() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isNoContent());
    }

    @Test
    void rutExistsReturnsTrueWhenUserRutExists() throws Exception {
        when(authService.rutExists("12.345.678-5")).thenReturn(true);

        mockMvc.perform(get("/auth/rut-exists")
                .param("rut", "12.345.678-5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));

        verify(authService).rutExists("12.345.678-5");
    }

    @Test
    void rutExistsReturnsFalseWhenUserRutDoesNotExist() throws Exception {
        when(authService.rutExists("12.345.678-5")).thenReturn(false);

        mockMvc.perform(get("/auth/rut-exists")
                .param("rut", "12.345.678-5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(false));

        verify(authService).rutExists("12.345.678-5");
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

        AuthLoginResult result = new AuthLoginResult(
                "Nombre", "Apellido", "jwt-token", "recepcionista", "sucursal", TALLER_ID, SUCURSAL_ID);
        when(authService.register(ArgumentMatchers.any(AuthRegisterCommand.class))).thenReturn(result);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.rol").value("recepcionista"))
            .andExpect(jsonPath("$.ambito").value("sucursal"))
            .andExpect(jsonPath("$.tallerId").value(TALLER_ID.toString()))
            .andExpect(jsonPath("$.sucursalId").value(SUCURSAL_ID.toString()));

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
    void resetPasswordWhenResendIsNotConfiguredReturnsServiceUnavailable() throws Exception {
        when(authService.resetPassword(eq("ana@veloservice.com"), anyString()))
                .thenThrow(new ServiceUnavailableException("Servicio de correo no disponible"));

        mockMvc.perform(post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@veloservice.com\"}"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.status").value(503))
            .andExpect(jsonPath("$.message").value("Servicio temporalmente no disponible"));
    }

    @Test
    void loginAdminTallerReturns200WithRole() throws Exception {
        AuthLoginResult result = new AuthLoginResult("Admin", "Taller",
                "eyJhbGciOiJIUzI1NiJ9.fake.sig", "admin_taller", "taller", TALLER_ID, null);
        when(authService.login(ArgumentMatchers.any(AuthLoginCommand.class))).thenReturn(result);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@taller.com\",\"password\":\"Password1!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("admin_taller"))
                .andExpect(jsonPath("$.ambito").value("taller"))
                .andExpect(jsonPath("$.tallerId").value(TALLER_ID.toString()))
                .andExpect(jsonPath("$.sucursalId").doesNotExist());
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

    @Test
    void changeMyPasswordCallsAuthenticatedPasswordChange() throws Exception {
        mockMvc.perform(post("/auth/me/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"actual\":\"Password1!\",\"nueva\":\"Newpass1!\"}"))
            .andExpect(status().isNoContent());

        verify(authService).changeCurrentUserPassword("Password1!", "Newpass1!");
    }
}
