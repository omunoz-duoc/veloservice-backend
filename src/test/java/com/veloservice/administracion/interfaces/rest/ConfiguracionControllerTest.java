package com.veloservice.administracion.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.application.dto.TallerResult;
import com.veloservice.administracion.application.usecase.TallerService;
import com.veloservice.config.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfiguracionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ConfiguracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TallerService tallerService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void obtenerTallerReturnsCurrentWorkshopData() throws Exception {
        when(tallerService.obtenerActual()).thenReturn(tallerResult());

        mockMvc.perform(get("/configuracion/taller"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("VeloService"))
                .andExpect(jsonPath("$.rut").value("76.845.210-3"))
                .andExpect(jsonPath("$.telefono").value("+56223211220"))
                .andExpect(jsonPath("$.email").value("contacto@veloservice.cl"))
                .andExpect(jsonPath("$.logoUrl").value("https://cdn.example/logo.png"));

        verify(tallerService).obtenerActual();
    }

    @Test
    void actualizarTallerPatchesCurrentWorkshopData() throws Exception {
        when(tallerService.actualizarActual(
                ArgumentMatchers.eq("VeloService"),
                ArgumentMatchers.eq("76.845.210-3"),
                ArgumentMatchers.eq("+56911112222"),
                ArgumentMatchers.eq("nuevo@veloservice.cl"),
                ArgumentMatchers.eq("https://cdn.example/logo.png")
        )).thenReturn(tallerResult("+56911112222", "nuevo@veloservice.cl"));

        mockMvc.perform(patch("/configuracion/taller")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nombre", "VeloService",
                                "rut", "76.845.210-3",
                                "telefono", "+56911112222",
                                "email", "nuevo@veloservice.cl",
                                "logoUrl", "https://cdn.example/logo.png"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value("+56911112222"))
                .andExpect(jsonPath("$.email").value("nuevo@veloservice.cl"));
    }

    private TallerResult tallerResult() {
        return tallerResult("+56223211220", "contacto@veloservice.cl");
    }

    private TallerResult tallerResult(String telefono, String email) {
        return TallerResult.builder()
                .id(UUID.randomUUID())
                .planId(UUID.randomUUID())
                .nombre("VeloService")
                .rut("76.845.210-3")
                .telefono(telefono)
                .email(email)
                .logoUrl("https://cdn.example/logo.png")
                .activo(true)
                .build();
    }
}
