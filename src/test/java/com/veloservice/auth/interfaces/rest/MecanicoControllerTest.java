package com.veloservice.auth.interfaces.rest;

import com.veloservice.auth.application.dto.MecanicoResult;
import com.veloservice.auth.application.usecase.MecanicoService;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MecanicoController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "admin_taller")
class MecanicoControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private MecanicoService mecanicoService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void listarActivosReturnsMechanics() throws Exception {
        UUID mecanicoId = UUID.randomUUID();
        when(mecanicoService.listarActivos()).thenReturn(List.of(
                new MecanicoResult(mecanicoId, "Diego", "Pizarro", "mecanico@andespedal.cl", "mecanico")
        ));

        mockMvc.perform(get("/mecanicos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(mecanicoId.toString()))
                .andExpect(jsonPath("$[0].nombre").value("Diego"))
                .andExpect(jsonPath("$[0].apellido").value("Pizarro"))
                .andExpect(jsonPath("$[0].email").value("mecanico@andespedal.cl"))
                .andExpect(jsonPath("$[0].rol").value("mecanico"));
    }

    @Test
    void listarActivosReturnsEmptyList() throws Exception {
        when(mecanicoService.listarActivos()).thenReturn(List.of());

        mockMvc.perform(get("/mecanicos/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
