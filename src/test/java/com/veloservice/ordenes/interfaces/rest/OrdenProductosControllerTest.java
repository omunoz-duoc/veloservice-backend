package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrdenProductosControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private OrdenService ordenService;
    @MockBean private OrdenRepository ordenRepository;
    @MockBean private OrdenProductoRepository ordenProductoRepository;
    @MockBean private MultimediaService multimediaService;
    @MockBean private MultimediaRepository multimediaRepository;
    @MockBean private ComentarioService comentarioService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void getProductosReturnsListForOrden() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();

        OrdenProductoResult r = OrdenProductoResult.builder()
                .id(UUID.randomUUID())
                .productoId(productoId)
                .nombre("Cadena Shimano HG601")
                .sku("SHM-HG601-11")
                .cantidad(1)
                .precioVenta(new BigDecimal("18900"))
                .build();

        when(ordenService.listarProductosPorOrden(ordenId)).thenReturn(List.of(r));

        mockMvc.perform(get("/ordenes/{id}/productos", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos[0].nombre").value("Cadena Shimano HG601"))
                .andExpect(jsonPath("$.productos[0].sku").value("SHM-HG601-11"))
                .andExpect(jsonPath("$.productos[0].cantidad").value(1));
    }

    @Disabled("Task 8: DELETE /ordenes/{id}/productos/{productoId} not yet implemented")
    @Test
    void deleteProductoReturnsOk() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();

        mockMvc.perform(delete("/ordenes/{id}/productos/{productoId}", ordenId, productoId))
                .andExpect(status().isOk());
    }
}
