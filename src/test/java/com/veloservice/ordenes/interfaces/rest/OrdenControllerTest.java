package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.OrdenDetalleResult;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.application.usecase.OrdenService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("unchecked")
class OrdenControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrdenService ordenService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(roles = "mecanico")
    void cambiarEstadoReturnsUpdatedDetalle() throws Exception {
        String ordenId = UUID.randomUUID().toString();
        UUID estadoId = UUID.randomUUID();
        when(ordenService.obtenerDetalle(ordenId)).thenReturn(detalle(UUID.fromString(ordenId), estadoId));

        mockMvc.perform(patch("/ordenes/{id}/estado", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "codigo", "en_diagnostico",
                                "observacion", "Diagnostico iniciado"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ordenId))
                .andExpect(jsonPath("$.estado.id").value(estadoId.toString()))
                .andExpect(jsonPath("$.estado.codigo").value("en_diagnostico"))
                .andExpect(jsonPath("$.estado.nombre").value("En diagnostico"));

        ArgumentCaptor<OrdenEstadoChangeCommand> captor = ArgumentCaptor.forClass(OrdenEstadoChangeCommand.class);
        verify(ordenService).cambiarEstado(eq(ordenId), captor.capture());
        assertThat(captor.getValue().getCodigo()).isEqualTo("en_diagnostico");
        assertThat(captor.getValue().getObservacion()).isEqualTo("Diagnostico iniciado");
        verify(ordenService).obtenerDetalle(ordenId);
    }

    @Test
    @WithMockUser(roles = "mecanico")
    void cambiarEstadoRejectsBlankCodigo() throws Exception {
        mockMvc.perform(patch("/ordenes/{id}/estado", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("codigo", " "))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenService);
    }

    @Test
    @WithMockUser(roles = "mecanico")
    void agregarProductosReturnsCreatedLines() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        when(ordenService.agregarProductos(eq(ordenId), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(List.of(new OrdenProductoResult(
                        lineId,
                        productoId,
                        "Pastillas de freno Shimano",
                        "SKU-001",
                        2,
                        new BigDecimal("12500.00")
                )));

        mockMvc.perform(post("/ordenes/{id}/productos", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(Map.of(
                                "productoId", productoId,
                                "cantidad", 2,
                                "proporcionadoPorCliente", false,
                                "notas", "Instalar"
                        )))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(lineId.toString()))
                .andExpect(jsonPath("$[0].productoId").value(productoId.toString()))
                .andExpect(jsonPath("$[0].nombre").value("Pastillas de freno Shimano"))
                .andExpect(jsonPath("$[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$[0].cantidad").value(2))
                .andExpect(jsonPath("$[0].precioVenta").value(12500.00));

        ArgumentCaptor<List<OrdenProductoAddCommand>> captor = ArgumentCaptor.forClass(List.class);
        verify(ordenService).agregarProductos(eq(ordenId), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().getFirst().getProductoId()).isEqualTo(productoId);
        assertThat(captor.getValue().getFirst().getCantidad()).isEqualTo(2);
        assertThat(captor.getValue().getFirst().getProporcionadoPorCliente()).isFalse();
        assertThat(captor.getValue().getFirst().getNotas()).isEqualTo("Instalar");
    }

    @Test
    @WithMockUser(roles = "mecanico")
    void agregarServiciosReturnsCreatedLines() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        when(ordenService.agregarServicios(eq(ordenId), org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(List.of(new OrdenServicioResult(
                        lineId,
                        servicioId,
                        "Ajuste de frenos",
                        new BigDecimal("8500.00")
                )));

        mockMvc.perform(post("/ordenes/{id}/servicios", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(Map.of(
                                "servicioId", servicioId,
                                "notas", "Delantero"
                        )))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(lineId.toString()))
                .andExpect(jsonPath("$[0].servicioId").value(servicioId.toString()))
                .andExpect(jsonPath("$[0].nombre").value("Ajuste de frenos"))
                .andExpect(jsonPath("$[0].precioBase").value(8500.00));

        ArgumentCaptor<List<OrdenServicioAddCommand>> captor = ArgumentCaptor.forClass(List.class);
        verify(ordenService).agregarServicios(eq(ordenId), captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().getFirst().getServicioId()).isEqualTo(servicioId);
        assertThat(captor.getValue().getFirst().getNotas()).isEqualTo("Delantero");
    }

    @Test
    @WithMockUser(roles = "mecanico")
    void agregarProductosRejectsEmptyList() throws Exception {
        mockMvc.perform(post("/ordenes/{id}/productos", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(ordenService);
    }

    private OrdenDetalleResult detalle(UUID ordenId, UUID estadoId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new OrdenDetalleResult(
                ordenId,
                "OT-2026-001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                estadoId,
                "en_diagnostico",
                "En diagnostico",
                UUID.randomUUID(),
                "reparacion",
                "Reparacion",
                now,
                null,
                null,
                "Diagnostico inicial",
                null,
                null,
                UUID.randomUUID(),
                "Trek",
                "Domane",
                "Ruta",
                "Rojo",
                "SN123",
                UUID.randomUUID(),
                "Matias",
                "Diaz",
                "+56912345678",
                "matias@email.com",
                "13.456.789-0",
                null,
                null,
                null,
                "media",
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }
}
