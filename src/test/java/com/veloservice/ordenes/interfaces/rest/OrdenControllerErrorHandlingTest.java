package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.OrdenCreateResult;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.shared.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "mecanico")
class OrdenControllerErrorHandlingTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrdenService ordenService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void obtenerReturnsNotFoundWhenOrderDoesNotExist() throws Exception {
        when(ordenService.obtenerDetalle("OT-404"))
                .thenThrow(new ResourceNotFoundException("Orden no encontrada"));

        mockMvc.perform(get("/ordenes/OT-404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Orden no encontrada"));
    }

    @Test
    void cambiarEstadoWithBlankCodigoReturnsBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("codigo", ""));

        mockMvc.perform(patch("/ordenes/OT-000001/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validacion fallida"))
                .andExpect(jsonPath("$.errors.codigo").exists());
    }

    @Test
    void agregarProductosWithInvalidUuidReturnsBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(List.of(Map.of(
                "productoId", UUID.randomUUID(),
                "cantidad", 1
        )));

        mockMvc.perform(post("/ordenes/no-es-uuid/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("id tiene un formato invalido"));
    }

    @Test
    void agregarProductosWhenAccessDeniedReturnsForbidden() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        String body = objectMapper.writeValueAsString(List.of(Map.of(
                "productoId", productoId,
                "cantidad", 1
        )));
        when(ordenService.agregarProductos(ArgumentMatchers.eq(ordenId), anyList()))
                .thenThrow(new AccessDeniedException("Acceso denegado"));

        mockMvc.perform(post("/ordenes/{id}/productos", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acceso denegado"));
    }

    @Test
    void agregarServiciosWhenInfrastructureFailsReturnsServiceUnavailable() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        String body = objectMapper.writeValueAsString(List.of(Map.of("servicioId", servicioId)));
        when(ordenService.agregarServicios(ArgumentMatchers.eq(ordenId), anyList()))
                .thenThrow(new DataAccessResourceFailureException("db down"));

        mockMvc.perform(post("/ordenes/{id}/servicios", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("Servicio temporalmente no disponible"));
    }

    @Test
    void crearWhenOrderNumberDuplicatedReturnsConflict() throws Exception {
        RuntimeException cause = new RuntimeException("idx_ordenes_taller_numero");
        when(ordenService.crear(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate order number", cause));

        mockMvc.perform(post("/ordenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Numero de orden duplicado. Intenta crear la orden nuevamente."));
    }

    @Test
    void crearWhenUnexpectedFailureReturnsInternalServerError() throws Exception {
        when(ordenService.crear(any()))
                .thenThrow(new IllegalStateException("Estado 'recibida' no configurado"));

        mockMvc.perform(post("/ordenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Error interno del servidor"));
    }

    @Test
    void crearSuccessStillReturnsCreated() throws Exception {
        UUID ordenId = UUID.randomUUID();
        when(ordenService.crear(any()))
                .thenReturn(new OrdenCreateResult(ordenId, "OT-000001"));

        mockMvc.perform(post("/ordenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ordenId.toString()))
                .andExpect(jsonPath("$.numeroOrden").value("OT-000001"));
    }

    private String validCreateJson() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "clienteId", UUID.randomUUID(),
                "bicicletaId", UUID.randomUUID(),
                "sucursalId", UUID.randomUUID(),
                "tipoTrabajo", "mantencion",
                "prioridad", "media"
        ));
    }
}
