package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.enums.TipoArchivoEnum;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.MultimediaResult;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class MultimediaOrdenControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrdenService ordenService;
    @MockBean private OrdenRepository ordenRepository;
    @MockBean private OrdenProductoRepository ordenProductoRepository;
    @MockBean private MultimediaService multimediaService;
    @MockBean private MultimediaRepository multimediaRepository;
    @MockBean private ComentarioService comentarioService;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private UsuarioRepository usuarioRepository;

    @Test
    void getMultimediaReturnsListForOrden() throws Exception {
        UUID ordenId = UUID.randomUUID();
        when(ordenService.resolveOrdenId(ordenId.toString())).thenReturn(ordenId);
        MultimediaResult m = MultimediaResult.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(UUID.randomUUID())
                .url("https://storage.example.com/foto1.jpg")
                .tipoArchivo(TipoArchivoEnum.imagen)
                .etapa(EtapaMultimediaEnum.ingreso)
                .createdAt(OffsetDateTime.now())
                .build();

        when(multimediaService.listarPorOrden(ordenId)).thenReturn(List.of(m));

        mockMvc.perform(get("/ordenes/{id}/multimedia", ordenId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.multimedia[0].url")
                        .value("https://storage.example.com/foto1.jpg"));
    }

    @Test
    void postMultimediaUploadsAndReturns() throws Exception {
        UUID ordenId = UUID.randomUUID();
        when(ordenService.resolveOrdenId(ordenId.toString())).thenReturn(ordenId);
        MultimediaResult m = MultimediaResult.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(UUID.randomUUID())
                .url("https://storage.example.com/foto2.jpg")
                .tipoArchivo(TipoArchivoEnum.imagen)
                .etapa(EtapaMultimediaEnum.ingreso)
                .createdAt(OffsetDateTime.now())
                .build();

        when(multimediaService.subir(eq(ordenId), eq("ingreso"), any())).thenReturn(m);

        mockMvc.perform(post("/ordenes/{id}/multimedia", ordenId)
                .param("etapa", "ingreso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "url", "https://storage.example.com/foto2.jpg",
                        "tipoArchivo", "imagen"
                ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://storage.example.com/foto2.jpg"));
    }

    @Test
    void deleteMultimediaReturnsOk() throws Exception {
        UUID ordenId = UUID.randomUUID();
        when(ordenService.resolveOrdenId(ordenId.toString())).thenReturn(ordenId);
        UUID mediaId = UUID.randomUUID();
        doNothing().when(multimediaService).eliminar(mediaId);

        mockMvc.perform(delete("/ordenes/{id}/multimedia/{mediaId}", ordenId, mediaId))
                .andExpect(status().isOk());
    }
}
