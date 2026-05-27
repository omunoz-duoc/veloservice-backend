package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrdenEstadosControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OrdenService ordenService;
    @MockBean private OrdenProductoRepository ordenProductoRepository;
    @MockBean private MultimediaService multimediaService;
    @MockBean private MultimediaRepository multimediaRepository;
    @MockBean private ComentarioService comentarioService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @BeforeEach
    void setUp() {
        UUID sucursalId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(usuarioId);
    }

    @Test
    void estadosReturnsFourGroups() throws Exception {
        when(ordenService.contarPorEstado()).thenReturn(Map.of(
            "recibida", 1L,
            "en_proceso", 2L,
            "lista_para_entrega", 1L,
            "entregada", 1L
        ));

        mockMvc.perform(get("/ordenes/estados"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recibida").value(1))
            .andExpect(jsonPath("$.en_proceso").value(2))
            .andExpect(jsonPath("$.lista_para_entrega").value(1))
            .andExpect(jsonPath("$.entregada").value(1));
    }
}
