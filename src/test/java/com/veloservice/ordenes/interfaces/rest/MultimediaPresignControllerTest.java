package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.ordenes.domain.EtapaMultimediaEnum;
import com.veloservice.ordenes.domain.TipoArchivoEnum;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.PresignResult;
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
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrdenController.class)
@AutoConfigureMockMvc(addFilters = false)
class MultimediaPresignControllerTest {

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
    void presignReturnsUploadUrlAndFileKey() throws Exception {
        UUID ordenId = UUID.randomUUID();
        String fileKey = "ordenes/" + ordenId + "/uuid.jpg";
        when(ordenService.resolveOrdenId(ordenId.toString())).thenReturn(ordenId);
        when(multimediaService.generarPresign(
                eq(ordenId), eq("foto.jpg"), eq("image/jpeg"), eq(1024L)))
                .thenReturn(new PresignResult("https://r2.example.com/upload?sig=abc", fileKey));

        mockMvc.perform(post("/ordenes/{id}/multimedia/presign", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fileName", "foto.jpg",
                                "contentType", "image/jpeg",
                                "fileSize", 1024
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uploadUrl").value("https://r2.example.com/upload?sig=abc"))
                .andExpect(jsonPath("$.fileKey").value(fileKey));
    }

    @Test
    void confirmSavesMultimediaAndReturnsResponse() throws Exception {
        UUID ordenId = UUID.randomUUID();
        String fileKey = "ordenes/" + ordenId + "/uuid.jpg";
        String publicUrl = "https://media.example.com/" + fileKey;

        when(ordenService.resolveOrdenId(ordenId.toString())).thenReturn(ordenId);
        MultimediaResult result = MultimediaResult.builder()
                .id(UUID.randomUUID())
                .ordenId(ordenId)
                .usuarioId(UUID.randomUUID())
                .url(publicUrl)
                .tipoArchivo(TipoArchivoEnum.imagen)
                .etapa(EtapaMultimediaEnum.ingreso)
                .createdAt(OffsetDateTime.now())
                .build();
        when(multimediaService.confirmar(
                eq(ordenId), eq("ingreso"), eq(fileKey),
                eq(TipoArchivoEnum.imagen), isNull()))
                .thenReturn(result);

        mockMvc.perform(post("/ordenes/{id}/multimedia/confirm", ordenId)
                        .param("etapa", "ingreso")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fileKey", fileKey,
                                "tipoArchivo", "imagen"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(publicUrl))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.ordenId").value(ordenId.toString()))
                .andExpect(jsonPath("$.tipoArchivo").value("imagen"));
    }
}
