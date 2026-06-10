package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.JwtTokenProvider;
import com.veloservice.ordenes.application.dto.OrdenCatalogoResult;
import com.veloservice.ordenes.application.dto.OrdenCreateResult;
import com.veloservice.ordenes.application.dto.OrdenDetalleResult;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.MultimediaConfirmationResult;
import com.veloservice.ordenes.application.dto.MultimediaPresignResult;
import com.veloservice.ordenes.application.dto.OrdenMetricasResult;
import com.veloservice.ordenes.application.dto.OrdenUpdateCommand;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.shared.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
    void metricasReturnsMobileCounters() throws Exception {
        when(ordenService.metricas(null)).thenReturn(new OrdenMetricasResult(3, 8, 2, 24));

        mockMvc.perform(get("/ordenes/metricas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recibidas").value(3))
                .andExpect(jsonPath("$.enProceso").value(8))
                .andExpect(jsonPath("$.listas").value(2))
                .andExpect(jsonPath("$.entregadas").value(24));
    }

    @Test
    void agregarComentarioTrimsInServiceAndReturnsTimelineRecord() throws Exception {
        UUID comentarioId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-08T11:40:00-04:00");
        when(ordenService.agregarComentario("OT-0343", "Comentario valido"))
                .thenReturn(new ComentarioResult(
                        comentarioId, usuarioId, "Rodrigo Soto", "Comentario valido", createdAt
                ));

        mockMvc.perform(post("/ordenes/OT-0343/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Comentario valido\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(comentarioId.toString()))
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.usuario").value("Rodrigo Soto"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-08T11:40:00-04:00"));
    }

    @Test
    void agregarComentarioRejectsBlankText() throws Exception {
        mockMvc.perform(post("/ordenes/OT-0343/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.texto").exists());
    }

    @Test
    void agregarMultimediaReturnsDetectedMetadata() throws Exception {
        UUID mediaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-08T11:05:00-04:00");
        byte[] jpeg = new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x01};
        when(ordenService.agregarMultimedia(
                ArgumentMatchers.eq("OT-0343"),
                ArgumentMatchers.any(byte[].class),
                ArgumentMatchers.eq("image/jpeg"),
                ArgumentMatchers.eq("Desgaste"),
                ArgumentMatchers.eq("diagnostico")
        )).thenReturn(new MultimediaResult(
                mediaId, usuarioId, "Rodrigo Soto", "image/jpeg", "imagen",
                "https://media.example/archivo.jpg", "diagnostico", "Desgaste", createdAt
        ));

        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile(
                        "file", "foto.jpg", "image/jpeg", jpeg
                );
        org.springframework.mock.web.MockMultipartFile tipoArchivo =
                new org.springframework.mock.web.MockMultipartFile(
                        "tipoArchivo", "", "text/plain", "image/jpeg".getBytes()
                );
        org.springframework.mock.web.MockMultipartFile descripcion =
                new org.springframework.mock.web.MockMultipartFile(
                        "descripcion", "", "text/plain", "Desgaste".getBytes()
                );
        org.springframework.mock.web.MockMultipartFile etapa =
                new org.springframework.mock.web.MockMultipartFile(
                        "etapa", "", "text/plain", "diagnostico".getBytes()
                );

        mockMvc.perform(multipart("/ordenes/OT-0343/multimedia")
                        .file(file)
                        .file(tipoArchivo)
                        .file(descripcion)
                        .file(etapa))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoArchivo").value("image/jpeg"))
                .andExpect(jsonPath("$.categoria").value("imagen"))
                .andExpect(jsonPath("$.url").value("https://media.example/archivo.jpg"))
                .andExpect(jsonPath("$.createdAt").value("2026-06-08T11:05:00-04:00"));
    }

    @Test
    void prepararMultimediaReturnsPresignedUploadContract() throws Exception {
        UUID ordenId = UUID.randomUUID();
        String objectKey = "ordenes/" + ordenId + "/" + UUID.randomUUID() + ".jpg";
        when(ordenService.prepararMultimedia(ordenId.toString(), "image/jpeg"))
                .thenReturn(new MultimediaPresignResult(
                        "https://r2.example/presigned",
                        objectKey,
                        "https://media.example/" + objectKey
                ));

        mockMvc.perform(post("/ordenes/{id}/multimedia/presign", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoArchivo":"image/jpeg","nombre":"foto.jpg"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presignedUrl").value("https://r2.example/presigned"))
                .andExpect(jsonPath("$.objectKey").value(objectKey))
                .andExpect(jsonPath("$.publicUrl").value("https://media.example/" + objectKey));
    }

    @Test
    void confirmarMultimediaReturnsCreatedRecord() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String objectKey = "ordenes/" + ordenId + "/" + UUID.randomUUID() + ".jpg";
        String publicUrl = "https://media.example/" + objectKey;
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-06-09T11:00:00-04:00");
        MultimediaResult multimedia = new MultimediaResult(
                mediaId, usuarioId, "Rodrigo Soto", "image/jpeg", "imagen",
                publicUrl, "diagnostico", "Desgaste", createdAt
        );
        when(ordenService.confirmarMultimedia(
                ordenId.toString(), objectKey, publicUrl, "image/jpeg", "Desgaste", "diagnostico"
        )).thenReturn(new MultimediaConfirmationResult(multimedia, true));

        mockMvc.perform(post("/ordenes/{id}/multimedia/confirm", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "objectKey", objectKey,
                                "publicUrl", publicUrl,
                                "tipoArchivo", "image/jpeg",
                                "descripcion", "Desgaste",
                                "etapa", "diagnostico"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mediaId.toString()))
                .andExpect(jsonPath("$.categoria").value("imagen"))
                .andExpect(jsonPath("$.url").value(publicUrl));
    }

    @Test
    void confirmarMultimediaReturnsOkWhenAlreadyConfirmed() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        String objectKey = "ordenes/" + ordenId + "/" + UUID.randomUUID() + ".pdf";
        String publicUrl = "https://media.example/" + objectKey;
        MultimediaResult multimedia = new MultimediaResult(
                mediaId, usuarioId, "Rodrigo Soto", "application/pdf", "documento",
                publicUrl, null, null, OffsetDateTime.now()
        );
        when(ordenService.confirmarMultimedia(
                ordenId.toString(), objectKey, publicUrl, "application/pdf", null, null
        )).thenReturn(new MultimediaConfirmationResult(multimedia, false));

        mockMvc.perform(post("/ordenes/{id}/multimedia/confirm", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "objectKey", objectKey,
                                "publicUrl", publicUrl,
                                "tipoArchivo", "application/pdf"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mediaId.toString()));
    }

    @Test
    void agregarProductosAcceptsNumeroOrden() throws Exception {
        UUID productoId = UUID.randomUUID();
        when(ordenService.agregarProductos(ArgumentMatchers.eq("OT-0343"), anyList()))
                .thenReturn(List.of());

        mockMvc.perform(post("/ordenes/OT-0343/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(Map.of(
                                "productoId", productoId,
                                "cantidad", 1
                        )))))
                .andExpect(status().isCreated());

        verify(ordenService).agregarProductos(ArgumentMatchers.eq("OT-0343"), anyList());
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

    @Test
    void listarEstadosCatalogoReturnsConfiguredStatuses() throws Exception {
        when(ordenService.listarEstadosCatalogo()).thenReturn(List.of(
                new OrdenCatalogoResult("recibida", "Recibida", 1, null),
                new OrdenCatalogoResult("entregada", "Entregada", 7, null)
        ));

        mockMvc.perform(get("/ordenes/catalogos/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("recibida"))
                .andExpect(jsonPath("$[0].nombre").value("Recibida"))
                .andExpect(jsonPath("$[0].orden").value(1))
                .andExpect(jsonPath("$[0].activo").doesNotExist())
                .andExpect(jsonPath("$[1].codigo").value("entregada"));
    }

    @Test
    void listarTiposCatalogoReturnsConfiguredTypes() throws Exception {
        when(ordenService.listarTiposCatalogo()).thenReturn(List.of(
                new OrdenCatalogoResult("mantencion", "Mantencion", null, true),
                new OrdenCatalogoResult("reparacion", "Reparacion", null, true)
        ));

        mockMvc.perform(get("/ordenes/catalogos/tipos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("mantencion"))
                .andExpect(jsonPath("$[0].nombre").value("Mantencion"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[1].codigo").value("reparacion"));
    }

    @Test
    void listarPrioridadesCatalogoIncludesUrgente() throws Exception {
        when(ordenService.listarPrioridadesCatalogo()).thenReturn(List.of(
                new OrdenCatalogoResult("baja", "Baja", null, true),
                new OrdenCatalogoResult("media", "Media", null, true),
                new OrdenCatalogoResult("alta", "Alta", null, true),
                new OrdenCatalogoResult("urgente", "Urgente", null, true)
        ));

        mockMvc.perform(get("/ordenes/catalogos/prioridades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].codigo")
                        .value(org.hamcrest.Matchers.contains("baja", "media", "alta", "urgente")));
    }

    @Test
    void obtenerDetalleMapsBicicletaFieldsInContractOrder() throws Exception {
        UUID ordenId = UUID.randomUUID();
        when(ordenService.obtenerDetalle("OT-000001"))
                .thenReturn(detalleResult(ordenId));

        mockMvc.perform(get("/ordenes/OT-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bicicleta.tipo").value("Ruta"))
                .andExpect(jsonPath("$.bicicleta.aro").value("700c"))
                .andExpect(jsonPath("$.bicicleta.color").value("Rojo"))
                .andExpect(jsonPath("$.bicicleta.numeroSerie").value("SN-001"));
    }

    @Test
    void obtenerDetalleReturnsProductEditableFields() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        when(ordenService.obtenerDetalle("OT-000001"))
                .thenReturn(detalleResult(ordenId, List.of(new OrdenProductoResult(
                        lineId,
                        productoId,
                        "Cadena Shimano",
                        "CAD-001",
                        2,
                        new BigDecimal("12500.00"),
                        new BigDecimal("11000.00"),
                        "Nota editable",
                        true
                ))));

        mockMvc.perform(get("/ordenes/OT-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productos[0].id").value(lineId.toString()))
                .andExpect(jsonPath("$.productos[0].productoId").value(productoId.toString()))
                .andExpect(jsonPath("$.productos[0].nombre").value("Cadena Shimano"))
                .andExpect(jsonPath("$.productos[0].sku").value("CAD-001"))
                .andExpect(jsonPath("$.productos[0].cantidad").value(2))
                .andExpect(jsonPath("$.productos[0].precioVenta").value(12500.00))
                .andExpect(jsonPath("$.productos[0].precioAplicado").value(11000.00))
                .andExpect(jsonPath("$.productos[0].notas").value("Nota editable"))
                .andExpect(jsonPath("$.productos[0].proporcionadoPorCliente").value(true));
    }

    @Test
    void actualizarMapsProductosCambiosToUnifiedCommand() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        UUID actualizarLineaId = UUID.randomUUID();
        UUID eliminarLineaId = UUID.randomUUID();
        when(ordenService.actualizar(ArgumentMatchers.eq(ordenId.toString()), any(OrdenUpdateCommand.class)))
                .thenReturn(detalleResult(ordenId));

        String body = objectMapper.writeValueAsString(Map.of(
                "estadoCodigo", "en_reparacion",
                "productosCambios", List.of(
                        Map.of(
                                "accion", "AGREGAR",
                                "productoId", productoId,
                                "cantidad", 1,
                                "notas", "opcional",
                                "proporcionadoPorCliente", false
                        ),
                        Map.of(
                                "accion", "ACTUALIZAR",
                                "lineaId", actualizarLineaId,
                                "cantidad", 2,
                                "notas", "nueva"
                        ),
                        Map.of(
                                "accion", "ELIMINAR",
                                "lineaId", eliminarLineaId
                        )
                )
        ));

        mockMvc.perform(patch("/ordenes/{id}", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<OrdenUpdateCommand> captor = ArgumentCaptor.forClass(OrdenUpdateCommand.class);
        verify(ordenService).actualizar(ArgumentMatchers.eq(ordenId.toString()), captor.capture());
        OrdenUpdateCommand command = captor.getValue();
        assert org.assertj.core.api.Assertions.assertThat(command.getEstadoCodigo()).isEqualTo("en_reparacion") != null;
        assert org.assertj.core.api.Assertions.assertThat(command.getProductosAgregar()).singleElement()
                .satisfies(item -> {
                    org.assertj.core.api.Assertions.assertThat(item.getProductoId()).isEqualTo(productoId);
                    org.assertj.core.api.Assertions.assertThat(item.getCantidad()).isEqualTo(1);
                    org.assertj.core.api.Assertions.assertThat(item.getNotas()).isEqualTo("opcional");
                    org.assertj.core.api.Assertions.assertThat(item.getProporcionadoPorCliente()).isFalse();
                }) != null;
        assert org.assertj.core.api.Assertions.assertThat(command.getProductosActualizar()).singleElement()
                .satisfies(item -> {
                    org.assertj.core.api.Assertions.assertThat(item.getId()).isEqualTo(actualizarLineaId);
                    org.assertj.core.api.Assertions.assertThat(item.getCantidad()).isEqualTo(2);
                    org.assertj.core.api.Assertions.assertThat(item.getNotas()).isEqualTo("nueva");
                }) != null;
        assert org.assertj.core.api.Assertions.assertThat(command.getProductosEliminar()).containsExactly(eliminarLineaId) != null;
    }

    @Test
    void actualizarMapsServiciosCambiosToUnifiedCommand() throws Exception {
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        UUID actualizarLineaId = UUID.randomUUID();
        UUID eliminarLineaId = UUID.randomUUID();
        when(ordenService.actualizar(ArgumentMatchers.eq(ordenId.toString()), any(OrdenUpdateCommand.class)))
                .thenReturn(detalleResult(ordenId));

        String body = objectMapper.writeValueAsString(Map.of(
                "serviciosCambios", List.of(
                        Map.of(
                                "accion", "AGREGAR",
                                "servicioId", servicioId,
                                "notas", "opcional"
                        ),
                        Map.of(
                                "accion", "ACTUALIZAR",
                                "lineaId", actualizarLineaId,
                                "precioAplicado", 12000,
                                "descuentoAplicado", 3000,
                                "notas", "nueva"
                        ),
                        Map.of(
                                "accion", "ELIMINAR",
                                "lineaId", eliminarLineaId
                        )
                )
        ));

        mockMvc.perform(patch("/ordenes/{id}", ordenId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        ArgumentCaptor<OrdenUpdateCommand> captor = ArgumentCaptor.forClass(OrdenUpdateCommand.class);
        verify(ordenService).actualizar(ArgumentMatchers.eq(ordenId.toString()), captor.capture());
        OrdenUpdateCommand command = captor.getValue();
        assert org.assertj.core.api.Assertions.assertThat(command.getServiciosAgregar()).singleElement()
                .satisfies(item -> {
                    org.assertj.core.api.Assertions.assertThat(item.getServicioId()).isEqualTo(servicioId);
                    org.assertj.core.api.Assertions.assertThat(item.getNotas()).isEqualTo("opcional");
                }) != null;
        assert org.assertj.core.api.Assertions.assertThat(command.getServiciosActualizar()).singleElement()
                .satisfies(item -> {
                    org.assertj.core.api.Assertions.assertThat(item.getId()).isEqualTo(actualizarLineaId);
                    org.assertj.core.api.Assertions.assertThat(item.getPrecioAplicado()).isEqualByComparingTo("12000");
                    org.assertj.core.api.Assertions.assertThat(item.getDescuentoAplicado()).isEqualByComparingTo("3000");
                    org.assertj.core.api.Assertions.assertThat(item.getNotas()).isEqualTo("nueva");
                }) != null;
        assert org.assertj.core.api.Assertions.assertThat(command.getServiciosEliminar()).containsExactly(eliminarLineaId) != null;
    }

    private OrdenDetalleResult detalleResult(UUID ordenId) {
        return detalleResult(ordenId, List.of());
    }

    private OrdenDetalleResult detalleResult(UUID ordenId, List<OrdenProductoResult> productos) {
        return new OrdenDetalleResult(
                ordenId,
                "OT-000001",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "recibida",
                "Recibida",
                UUID.randomUUID(),
                "mantencion",
                "Mantencion",
                null,
                null,
                null,
                "Diagnostico",
                null,
                null,
                UUID.randomUUID(),
                "Trek",
                "Domane",
                "Ruta",
                "Rojo",
                "SN-001",
                "700c",
                2024,
                "https://cdn.example/bici.jpg",
                "Notas",
                UUID.randomUUID(),
                "Cliente",
                "Demo",
                "+569",
                "cliente@example.com",
                "11.111.111-1",
                null,
                null,
                null,
                "media",
                List.of(),
                List.of(),
                productos,
                List.of()
        );
    }

    private String validCreateJson() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "clienteId", UUID.randomUUID(),
                "bicicletaId", UUID.randomUUID(),
                "sucursalId", UUID.randomUUID(),
                "tipoTrabajo", UUID.randomUUID(),
                "prioridad", "media"
        ));
    }
}
