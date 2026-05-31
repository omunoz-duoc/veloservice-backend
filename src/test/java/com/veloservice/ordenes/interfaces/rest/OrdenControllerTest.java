package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.application.usecase.OrdenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrdenControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OrdenService ordenService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void listarReturnsOrdenesWrapper() throws Exception {
        given(ordenService.listar()).willReturn(List.of(orden("OT-100")));

        mockMvc.perform(get("/ordenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.ordenes[0].numeroOrden").value("OT-100"))
                .andExpect(jsonPath("$.ordenes[0].estado.codigo").value("recibida"))
                .andExpect(jsonPath("$.ordenes[0].tipo.codigo").value("reparacion"))
                .andExpect(jsonPath("$.ordenes[0].bicicleta.marca").value("Trek"))
                .andExpect(jsonPath("$.ordenes[0].cliente.nombre").value("Ana"));
    }

    @Test
    void obtenerReturnsOneOrden() throws Exception {
        UUID ordenId = UUID.randomUUID();
        given(ordenService.obtener(ordenId.toString())).willReturn(orden("OT-101"));

        mockMvc.perform(get("/ordenes/{id}", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroOrden").value("OT-101"))
                .andExpect(jsonPath("$.mecanico.nombre").value("Luis"));
    }

    private OrdenReadResult orden(String numeroOrden) {
        return new OrdenReadResult(
                UUID.randomUUID(),
                numeroOrden,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "recibida",
                "Recibida",
                UUID.randomUUID(),
                "reparacion",
                "Reparacion",
                OffsetDateTime.parse("2026-05-30T10:00:00Z"),
                null,
                null,
                "Revision inicial",
                null,
                "Cliente espera presupuesto",
                UUID.randomUUID(),
                "Trek",
                "Marlin",
                "MTB",
                "29",
                "Negro",
                "SN-100",
                UUID.randomUUID(),
                "Ana",
                "Perez",
                "+56912345678",
                "ana@example.com",
                "11111111-1",
                UUID.randomUUID(),
                "Luis",
                "Gomez"
        );
    }
}
