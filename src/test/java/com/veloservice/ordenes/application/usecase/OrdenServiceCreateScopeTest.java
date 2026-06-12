package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioSucursalRepository;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.clientes.application.usecase.BicicletaService;
import com.veloservice.clientes.application.usecase.ClienteService;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.config.storage.R2Properties;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.OrdenDetalleBaseResult;
import com.veloservice.ordenes.application.dto.OrdenDetalleResult;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.OrdenProductoUpdateCommand;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.application.dto.OrdenServicioUpdateCommand;
import com.veloservice.ordenes.application.dto.OrdenUpdateCommand;
import com.veloservice.ordenes.domain.AccionHistorialEnum;
import com.veloservice.ordenes.domain.PrioridadOrdenEnum;
import com.veloservice.ordenes.domain.model.EstadoOrden;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenEstado;
import com.veloservice.ordenes.domain.model.OrdenProducto;
import com.veloservice.ordenes.domain.model.OrdenServicio;
import com.veloservice.ordenes.domain.model.Multimedia;
import com.veloservice.ordenes.application.port.R2StoragePort;
import com.veloservice.ordenes.domain.model.TipoOrden;
import com.veloservice.ordenes.infraestructure.persistence.repository.ComentarioRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.EstadoOrdenCatalogRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenEstadoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.TipoOrdenRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.servicios.domain.model.Servicio;
import com.veloservice.servicios.domain.model.SucursalServicio;
import com.veloservice.servicios.infraestructure.persistence.repository.SucursalServicioRepository;
import com.veloservice.shared.application.exception.BadRequestException;
import com.veloservice.shared.application.exception.ConflictException;
import com.veloservice.shared.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class OrdenServiceCreateScopeTest {

    @Mock private OrdenRepository ordenRepository;
    @Mock private ComentarioRepository comentarioRepository;
    @Mock private MultimediaRepository multimediaRepository;
    @Mock private EstadoOrdenCatalogRepository estadoOrdenRepository;
    @Mock private OrdenEstadoRepository ordenEstadoRepository;
    @Mock private TipoOrdenRepository tipoOrdenRepository;
    @Mock private ServicioRepository servicioRepository;
    @Mock private SucursalServicioRepository sucursalServicioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private OrdenServicioRepository ordenServicioRepository;
    @Mock private OrdenProductoRepository ordenProductoRepository;
    @Mock private SecuenciaService secuenciaService;
    @Mock private ClienteService clienteService;
    @Mock private BicicletaService bicicletaService;
    @Mock private BicicletaRepository bicicletaRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioSucursalRepository usuarioSucursalRepository;
    @Mock private R2StoragePort r2Storage;
    @Mock private OrdenHistorialService ordenHistorialService;

    private OrdenService ordenService;

    @BeforeEach
    void setUp() {
        ordenService = new OrdenService(
                ordenRepository,
                comentarioRepository,
                multimediaRepository,
                estadoOrdenRepository,
                ordenEstadoRepository,
                tipoOrdenRepository,
                servicioRepository,
                sucursalServicioRepository,
                productoRepository,
                ordenServicioRepository,
                ordenProductoRepository,
                secuenciaService,
                clienteService,
                bicicletaService,
                bicicletaRepository,
                clienteRepository,
                sucursalRepository,
                usuarioRepository,
                usuarioSucursalRepository,
                null,
                r2Storage,
                new R2Properties(
                        "test-account",
                        "test-key",
                        "test-secret",
                        "test-bucket",
                        "https://media.example",
                        Duration.ofMinutes(15)
                ),
                ordenHistorialService
        );
    }

    @AfterEach
    void cleanup() {
        TallerContext.clear();
        SucursalContext.clear();
        UsuarioContext.clear();
    }

    @Test
    void adminTallerCreatesOrderInRequestedSucursal() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID bicicletaId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(sucursalRepository.existsByIdAndTallerId(sucursalId, tallerId)).willReturn(true);
        given(clienteRepository.existsByIdAndTallerId(clienteId, tallerId)).willReturn(true);
        given(bicicletaRepository.findById(bicicletaId))
                .willReturn(Optional.of(Bicicleta.builder().id(bicicletaId).clienteId(clienteId).build()));
        given(estadoOrdenRepository.findByCodigo("recibida"))
                .willReturn(Optional.of(EstadoOrden.builder().id(UUID.randomUUID()).codigo("recibida").build()));
        UUID tipoId = UUID.fromString("31000000-0000-4000-8000-000000000002");
        given(tipoOrdenRepository.findById(tipoId))
                .willReturn(Optional.of(TipoOrden.builder().id(tipoId).codigo("mantencion").build()));
        given(secuenciaService.generarNumeroOrden(tallerId)).willReturn("OT-000001");
        given(ordenRepository.save(any(Orden.class))).willAnswer(invocation -> {
            Orden orden = invocation.getArgument(0);
            orden.setId(UUID.randomUUID());
            return orden;
        });

        ordenService.crear(baseCommand(clienteId, bicicletaId, sucursalId));

        ArgumentCaptor<Orden> captor = ArgumentCaptor.forClass(Orden.class);
        verify(ordenRepository).save(captor.capture());
        assertThat(captor.getValue().getTallerId()).isEqualTo(tallerId);
        assertThat(captor.getValue().getSucursalId()).isEqualTo(sucursalId);
        assertThat(SucursalContext.getCurrentSucursal()).isNull();
    }

    @Test
    void adminTallerMustSendSucursalId() {
        TallerContext.setCurrentTaller(UUID.randomUUID());

        assertThatThrownBy(() -> ordenService.crear(baseCommand(UUID.randomUUID(), UUID.randomUUID(), null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Se requiere sucursalId para crear una orden desde contexto de taller");
    }

    @Test
    void sucursalScopedUserCannotOverrideSucursal() {
        UUID tallerId = UUID.randomUUID();
        UUID contextSucursalId = UUID.randomUUID();
        UUID requestedSucursalId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        SucursalContext.setCurrentSucursal(contextSucursalId);

        assertThatThrownBy(() -> ordenService.crear(baseCommand(UUID.randomUUID(), UUID.randomUUID(), requestedSucursalId)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("La sucursal solicitada no coincide con el contexto actual");
    }

    @Test
    void cambiarEstadoUpdatesOrdenAndCreatesHistoryWithCurrentUser() {
        UUID sucursalId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID estadoAnteriorId = UUID.randomUUID();
        UUID estadoNuevoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(usuarioId);
        Orden orden = Orden.builder()
                .id(ordenId)
                .sucursalId(sucursalId)
                .estadoId(estadoAnteriorId)
                .build();
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(estadoOrdenRepository.findByCodigo("en_diagnostico"))
                .willReturn(Optional.of(EstadoOrden.builder().id(estadoNuevoId).codigo("en_diagnostico").build()));

        ordenService.cambiarEstado(ordenId.toString(), new OrdenEstadoChangeCommand("en_diagnostico", "Diagnostico iniciado"));

        assertThat(orden.getEstadoId()).isEqualTo(estadoNuevoId);
        assertThat(orden.getUpdatedAt()).isNotNull();
        verify(ordenRepository).save(orden);
        ArgumentCaptor<OrdenEstado> historyCaptor = ArgumentCaptor.forClass(OrdenEstado.class);
        verify(ordenEstadoRepository).save(historyCaptor.capture());
        OrdenEstado history = historyCaptor.getValue();
        assertThat(history.getOrdenId()).isEqualTo(ordenId);
        assertThat(history.getUsuarioId()).isEqualTo(usuarioId);
        assertThat(history.getEstadoAnteriorId()).isEqualTo(estadoAnteriorId);
        assertThat(history.getEstadoNuevoId()).isEqualTo(estadoNuevoId);
        assertThat(history.getObservacion()).isEqualTo("Diagnostico iniciado");
        assertThat(history.getCreatedAt()).isNotNull();
        verify(ordenHistorialService).registrar(eq(ordenId), eq(AccionHistorialEnum.ESTADO_CAMBIADO), eq("orden"), isNull(), any());
    }

    @Test
    void cambiarEstadoRequiresUsuarioContext() {
        SucursalContext.setCurrentSucursal(UUID.randomUUID());

        assertThatThrownBy(() -> ordenService.cambiarEstado("OT-000001", new OrdenEstadoChangeCommand("en_diagnostico", null)))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Contexto de usuario requerido");

        verifyNoInteractions(ordenEstadoRepository);
    }

    @Test
    void cambiarEstadoRejectsUnknownEstadoCodigo() {
        UUID sucursalId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(usuarioId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).estadoId(UUID.randomUUID()).build()));
        given(estadoOrdenRepository.findByCodigo("no_existe")).willReturn(Optional.empty());

        assertThatThrownBy(() -> ordenService.cambiarEstado(ordenId.toString(), new OrdenEstadoChangeCommand("no_existe", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Estado de orden no encontrado: no_existe");

        verifyNoInteractions(ordenEstadoRepository);
    }

    @Test
    void actualizarWithEstadoCodigoUpdatesStateAndCreatesHistory() {
        UUID sucursalId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID estadoAnteriorId = UUID.randomUUID();
        UUID estadoNuevoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(usuarioId);
        Orden orden = Orden.builder()
                .id(ordenId)
                .sucursalId(sucursalId)
                .estadoId(estadoAnteriorId)
                .build();
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(estadoOrdenRepository.findByCodigo("esperando_repuestos"))
                .willReturn(Optional.of(EstadoOrden.builder().id(estadoNuevoId).codigo("esperando_repuestos").build()));
        stubDetalle(ordenId, sucursalId, estadoNuevoId, UUID.randomUUID(), null, null);

        ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                "esperando_repuestos",
                "Cambio de estado desde panel web",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(orden.getEstadoId()).isEqualTo(estadoNuevoId);
        assertThat(orden.getUpdatedAt()).isNotNull();
        verify(ordenRepository).save(orden);
        ArgumentCaptor<OrdenEstado> historyCaptor = ArgumentCaptor.forClass(OrdenEstado.class);
        verify(ordenEstadoRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getOrdenId()).isEqualTo(ordenId);
        assertThat(historyCaptor.getValue().getUsuarioId()).isEqualTo(usuarioId);
        assertThat(historyCaptor.getValue().getEstadoAnteriorId()).isEqualTo(estadoAnteriorId);
        assertThat(historyCaptor.getValue().getEstadoNuevoId()).isEqualTo(estadoNuevoId);
        assertThat(historyCaptor.getValue().getObservacion()).isEqualTo("Cambio de estado desde panel web");
    }

    @Test
    void actualizarWithFullPayloadPersistsEditableFields() {
        UUID sucursalId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID estadoAnteriorId = UUID.randomUUID();
        UUID estadoNuevoId = UUID.randomUUID();
        UUID tipoNuevoId = UUID.randomUUID();
        UUID mecanicoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(usuarioId);
        Orden orden = Orden.builder()
                .id(ordenId)
                .sucursalId(sucursalId)
                .estadoId(estadoAnteriorId)
                .prioridad("media")
                .build();
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(estadoOrdenRepository.findByCodigo("esperando_repuestos"))
                .willReturn(Optional.of(EstadoOrden.builder().id(estadoNuevoId).codigo("esperando_repuestos").build()));
        given(tipoOrdenRepository.findByCodigo("revision"))
                .willReturn(Optional.of(TipoOrden.builder().id(tipoNuevoId).codigo("revision").build()));
        given(usuarioRepository.existsActiveMecanicoById(mecanicoId)).willReturn(true);
        stubDetalle(ordenId, sucursalId, estadoNuevoId, tipoNuevoId, mecanicoId, "baja");

        ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                "esperando_repuestos",
                "Cambio de estado desde panel web",
                "revision",
                "baja",
                mecanicoId,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(orden.getEstadoId()).isEqualTo(estadoNuevoId);
        assertThat(orden.getTipoId()).isEqualTo(tipoNuevoId);
        assertThat(orden.getPrioridad()).isEqualTo("baja");
        assertThat(orden.getMecanicoId()).isEqualTo(mecanicoId);
        verify(ordenRepository).save(orden);
        verify(ordenEstadoRepository).save(any(OrdenEstado.class));
        verify(ordenHistorialService).registrar(eq(ordenId), eq(AccionHistorialEnum.ORDEN_EDITADA), eq("orden"), isNull(), any());
    }

    @Test
    void actualizarAcceptsUrgentePriority() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID estadoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Orden orden = Orden.builder()
                .id(ordenId)
                .sucursalId(sucursalId)
                .estadoId(estadoId)
                .tipoId(tipoId)
                .prioridad("media")
                .build();
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(orden));
        stubDetalle(ordenId, sucursalId, estadoId, tipoId, null, "urgente");

        ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                "urgente",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(orden.getPrioridad()).isEqualTo("urgente");
        verify(ordenRepository).save(orden);
    }

    @Test
    void catalogosReturnEstadosTiposAndSupportedPrioridades() {
        given(estadoOrdenRepository.findAllByOrderByOrdenAsc()).willReturn(List.of(
                EstadoOrden.builder().codigo("recibida").nombre("Recibida").orden(1).esFinal(false).build(),
                EstadoOrden.builder().codigo("entregada").nombre("Entregada").orden(7).esFinal(true).build()
        ));
        given(tipoOrdenRepository.findAllByOrderByCodigoAsc()).willReturn(List.of(
                TipoOrden.builder().codigo("mantencion").nombre("Mantencion").activo(true).build(),
                TipoOrden.builder().codigo("reparacion").nombre("Reparacion").activo(true).build()
        ));

        assertThat(ordenService.listarEstadosCatalogo())
                .extracting("codigo")
                .containsExactly("recibida", "entregada");
        assertThat(ordenService.listarTiposCatalogo())
                .extracting("codigo")
                .containsExactly("mantencion", "reparacion");
        assertThat(ordenService.listarPrioridadesCatalogo())
                .extracting("codigo")
                .containsExactly("baja", "media", "alta", "urgente");
    }

    @Test
    void actualizarWithProductosAgregarPersistsLineItemsAndReturnsUpdatedDetail() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID estadoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Orden orden = Orden.builder()
                .id(ordenId)
                .sucursalId(sucursalId)
                .estadoId(estadoId)
                .tipoId(tipoId)
                .prioridad("media")
                .build();
        Producto producto = Producto.builder()
                .id(productoId)
                .sucursalId(sucursalId)
                .precioCosto(new BigDecimal("7000.00"))
                .precioVenta(new BigDecimal("12500.00"))
                .activo(true)
                .build();
        OrdenProductoResult productoResult = new OrdenProductoResult(
                lineId,
                productoId,
                "Cadena Shimano HG601 11v",
                "CAD-001",
                2,
                new BigDecimal("12500.00"),
                new BigDecimal("12500.00"),
                "Instalar desde drawer",
                false
        );
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(productoRepository.findById(productoId)).willReturn(Optional.of(producto));
        given(ordenProductoRepository.saveAll(anyList())).willAnswer(invocation -> {
            List<OrdenProducto> saved = invocation.getArgument(0);
            saved.getFirst().setId(lineId);
            return saved;
        });
        given(ordenProductoRepository.findResultByIdIn(List.of(lineId))).willReturn(List.of(productoResult));
        stubDetalle(ordenId, sucursalId, estadoId, tipoId, null, "media");
        given(ordenProductoRepository.findResultByOrdenId(ordenId)).willReturn(List.of(productoResult));

        OrdenDetalleResult result = ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                List.of(new OrdenProductoAddCommand(productoId, 2, false, "Instalar desde drawer")),
                null,
                null,
                null,
                null,
                null
        ));

        assertThat(result.productos()).containsExactly(productoResult);
        assertThat(orden.getUpdatedAt()).isNotNull();
        verify(ordenRepository).save(orden);
        ArgumentCaptor<List<OrdenProducto>> captor = ArgumentCaptor.forClass(List.class);
        verify(ordenProductoRepository).saveAll(captor.capture());
        OrdenProducto saved = captor.getValue().getFirst();
        assertThat(saved.getOrdenId()).isEqualTo(ordenId);
        assertThat(saved.getProductoId()).isEqualTo(productoId);
        assertThat(saved.getCantidad()).isEqualTo(2);
        assertThat(saved.getPrecioCostoSnapshot()).isEqualByComparingTo("7000.00");
        assertThat(saved.getPrecioVentaSnapshot()).isEqualByComparingTo("12500.00");
        assertThat(saved.getPrecioAplicado()).isEqualByComparingTo("12500.00");
        assertThat(saved.getProporcionadoPorCliente()).isFalse();
        assertThat(saved.getNotas()).isEqualTo("Instalar desde drawer");
    }

    @Test
    void actualizarWithProductosAgregarRejectsInvalidQuantity() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).sucursalId(sucursalId).build()));

        assertThatThrownBy(() -> ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                List.of(new OrdenProductoAddCommand(productoId, 0, false, null)),
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Cantidad de producto debe ser mayor o igual a 1");

        verifyNoInteractions(productoRepository);
        verifyNoInteractions(ordenProductoRepository);
    }

    @Test
    void actualizarWithProductosActualizarEditsQuantityAndNotesKeepingSnapshots() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID estadoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Orden orden = Orden.builder().id(ordenId).sucursalId(sucursalId).estadoId(estadoId).tipoId(tipoId).build();
        OrdenProducto lineItem = OrdenProducto.builder()
                .id(lineId)
                .ordenId(ordenId)
                .productoId(productoId)
                .cantidad(1)
                .precioCostoSnapshot(new BigDecimal("7000.00"))
                .precioVentaSnapshot(new BigDecimal("12500.00"))
                .precioAplicado(new BigDecimal("12500.00"))
                .proporcionadoPorCliente(false)
                .notas("Nota original")
                .build();
        Producto producto = Producto.builder()
                .id(productoId)
                .sucursalId(sucursalId)
                .activo(true)
                .stock(5)
                .build();
        OrdenProductoResult productoResult = new OrdenProductoResult(
                lineId,
                productoId,
                "Cadena Shimano HG601 11v",
                "CAD-001",
                3,
                new BigDecimal("12500.00"),
                new BigDecimal("12500.00"),
                "Nueva nota",
                true
        );
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(ordenProductoRepository.findByIdAndOrdenId(lineId, ordenId)).willReturn(Optional.of(lineItem));
        given(productoRepository.findById(productoId)).willReturn(Optional.of(producto));
        stubDetalle(ordenId, sucursalId, estadoId, tipoId, null, "media");
        given(ordenProductoRepository.findResultByOrdenId(ordenId)).willReturn(List.of(productoResult));

        OrdenDetalleResult result = ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new OrdenProductoUpdateCommand(lineId, 3, true, "Nueva nota")),
                null,
                null,
                null,
                null
        ));

        assertThat(result.productos()).containsExactly(productoResult);
        assertThat(lineItem.getCantidad()).isEqualTo(3);
        assertThat(lineItem.getNotas()).isEqualTo("Nueva nota");
        assertThat(lineItem.getProporcionadoPorCliente()).isTrue();
        assertThat(lineItem.getPrecioVentaSnapshot()).isEqualByComparingTo("12500.00");
        assertThat(lineItem.getPrecioAplicado()).isEqualByComparingTo("12500.00");
        verify(ordenProductoRepository).saveAll(List.of(lineItem));
        verify(ordenRepository).save(orden);
        verify(ordenHistorialService).registrar(eq(ordenId), eq(AccionHistorialEnum.PRODUCTO_MODIFICADO), eq("producto"), eq(productoId), any());
    }

    @Test
    void actualizarWithProductosEliminarDeletesLineItemsFromOrder() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID estadoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Orden orden = Orden.builder().id(ordenId).sucursalId(sucursalId).estadoId(estadoId).tipoId(tipoId).build();
        OrdenProducto lineItem = OrdenProducto.builder()
                .id(lineId)
                .ordenId(ordenId)
                .productoId(productoId)
                .cantidad(1)
                .build();
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(ordenProductoRepository.deleteByIdAndOrdenId(lineId, ordenId)).willReturn(1);
        stubDetalle(ordenId, sucursalId, estadoId, tipoId, null, "media");

        ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(lineId),
                null,
                null,
                null
        ));

        verify(ordenProductoRepository).deleteByIdAndOrdenId(lineId, ordenId);
        verify(ordenRepository).save(orden);
        verify(ordenHistorialService).registrar(eq(ordenId), eq(AccionHistorialEnum.PRODUCTO_QUITADO), eq("producto"), isNull(), any());
    }

    @Test
    void actualizarWithServiciosAgregarPersistsLineItemsAndReturnsUpdatedDetail() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID estadoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Orden orden = Orden.builder()
                .id(ordenId)
                .tallerId(tallerId)
                .sucursalId(sucursalId)
                .estadoId(estadoId)
                .tipoId(tipoId)
                .build();
        Servicio servicio = Servicio.builder()
                .id(servicioId)
                .tallerId(tallerId)
                .precioBase(new BigDecimal("15000.00"))
                .activo(true)
                .build();
        OrdenServicioResult servicioResult = new OrdenServicioResult(
                lineId,
                servicioId,
                "Ajuste general",
                new BigDecimal("15000.00"),
                new BigDecimal("15000.00"),
                BigDecimal.ZERO,
                "Servicio desde drawer"
        );
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(ordenServicioRepository.findByOrdenIdAndServicioId(ordenId, servicioId)).willReturn(Optional.empty());
        given(servicioRepository.findById(servicioId)).willReturn(Optional.of(servicio));
        given(sucursalServicioRepository.findBySucursalIdAndServicioId(sucursalId, servicioId)).willReturn(Optional.empty());
        given(ordenServicioRepository.saveAll(anyList())).willAnswer(invocation -> {
            List<OrdenServicio> saved = invocation.getArgument(0);
            saved.getFirst().setId(lineId);
            return saved;
        });
        given(ordenServicioRepository.findResultByIdIn(List.of(lineId))).willReturn(List.of(servicioResult));
        stubDetalle(ordenId, sucursalId, estadoId, tipoId, null, "media");
        given(ordenServicioRepository.findResultByOrdenId(ordenId)).willReturn(List.of(servicioResult));

        OrdenDetalleResult result = ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new OrdenServicioAddCommand(servicioId, "Servicio desde drawer")),
                null,
                null
        ));

        assertThat(result.servicios()).containsExactly(servicioResult);
        verify(ordenServicioRepository).saveAll(anyList());
        verify(ordenRepository).save(orden);
    }

    @Test
    void actualizarWithServiciosActualizarEditsNotesAndPrices() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID estadoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Orden orden = Orden.builder().id(ordenId).sucursalId(sucursalId).estadoId(estadoId).tipoId(tipoId).build();
        OrdenServicio lineItem = OrdenServicio.builder()
                .id(lineId)
                .ordenId(ordenId)
                .servicioId(servicioId)
                .precioBaseSnapshot(new BigDecimal("15000.00"))
                .precioAplicado(new BigDecimal("15000.00"))
                .descuentoAplicado(BigDecimal.ZERO)
                .notas("Nota original")
                .build();
        OrdenServicioResult servicioResult = new OrdenServicioResult(
                lineId,
                servicioId,
                "Ajuste general",
                new BigDecimal("15000.00"),
                new BigDecimal("12000.00"),
                new BigDecimal("3000.00"),
                "Nueva nota"
        );
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(ordenServicioRepository.findByIdAndOrdenId(lineId, ordenId)).willReturn(Optional.of(lineItem));
        stubDetalle(ordenId, sucursalId, estadoId, tipoId, null, "media");
        given(ordenServicioRepository.findResultByOrdenId(ordenId)).willReturn(List.of(servicioResult));

        OrdenDetalleResult result = ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new OrdenServicioUpdateCommand(lineId, new BigDecimal("12000.00"), new BigDecimal("3000.00"), "Nueva nota")),
                null
        ));

        assertThat(result.servicios()).containsExactly(servicioResult);
        assertThat(lineItem.getPrecioAplicado()).isEqualByComparingTo("12000.00");
        assertThat(lineItem.getDescuentoAplicado()).isEqualByComparingTo("3000.00");
        assertThat(lineItem.getNotas()).isEqualTo("Nueva nota");
        verify(ordenServicioRepository).saveAll(List.of(lineItem));
        verify(ordenRepository).save(orden);
        verify(ordenHistorialService).registrar(eq(ordenId), eq(AccionHistorialEnum.SERVICIO_MODIFICADO), eq("servicio"), eq(servicioId), any());
    }

    @Test
    void actualizarWithServiciosEliminarDeletesLineItemsFromOrder() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID estadoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Orden orden = Orden.builder().id(ordenId).sucursalId(sucursalId).estadoId(estadoId).tipoId(tipoId).build();
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(ordenServicioRepository.deleteByIdAndOrdenId(lineId, ordenId)).willReturn(1);
        stubDetalle(ordenId, sucursalId, estadoId, tipoId, null, "media");

        ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(lineId)
        ));

        verify(ordenServicioRepository).deleteByIdAndOrdenId(lineId, ordenId);
        verify(ordenRepository).save(orden);
        verify(ordenHistorialService).registrar(eq(ordenId), eq(AccionHistorialEnum.SERVICIO_QUITADO), eq("servicio"), isNull(), any());
    }

    @Test
    void actualizarWithServiciosEliminarRejectsLineFromAnotherOrder() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).sucursalId(sucursalId).build()));
        given(ordenServicioRepository.deleteByIdAndOrdenId(lineId, ordenId)).willReturn(0);

        assertThatThrownBy(() -> ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(lineId)
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Servicio asociado no encontrado: " + lineId);
    }

    @Test
    void actualizarWithServiciosAgregarRejectsServiceFromAnotherTaller() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId).build()));
        given(ordenServicioRepository.findByOrdenIdAndServicioId(ordenId, servicioId)).willReturn(Optional.empty());
        given(servicioRepository.findById(servicioId))
                .willReturn(Optional.of(Servicio.builder().id(servicioId).tallerId(UUID.randomUUID()).activo(true).build()));

        assertThatThrownBy(() -> ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new OrdenServicioAddCommand(servicioId, null)),
                null,
                null
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Servicio " + servicioId + " no pertenece al taller de esta orden");
    }

    @Test
    void actualizarWithServiciosAgregarRejectsDuplicateService() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId).build()));
        given(ordenServicioRepository.findByOrdenIdAndServicioId(ordenId, servicioId))
                .willReturn(Optional.of(OrdenServicio.builder().id(lineId).ordenId(ordenId).servicioId(servicioId).build()));

        assertThatThrownBy(() -> ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(new OrdenServicioAddCommand(servicioId, null)),
                null,
                null
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Servicio ya asociado a la orden: " + servicioId);
    }

    @Test
    void actualizarWithProductosAgregarRejectsInsufficientStock() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).sucursalId(sucursalId).build()));
        given(productoRepository.findById(productoId))
                .willReturn(Optional.of(Producto.builder()
                        .id(productoId)
                        .sucursalId(sucursalId)
                        .activo(true)
                        .stock(1)
                        .build()));

        assertThatThrownBy(() -> ordenService.actualizar(ordenId.toString(), new OrdenUpdateCommand(
                null,
                null,
                null,
                null,
                null,
                List.of(new OrdenProductoAddCommand(productoId, 2, false, null)),
                null,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Stock insuficiente para producto " + productoId + "; disponible: 1");

        verifyNoInteractions(ordenProductoRepository);
    }

    @Test
    void agregarProductosStoresSnapshotsAndReturnsCreatedRows() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Orden orden = Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId).build();
        Producto producto = Producto.builder()
                .id(productoId)
                .sucursalId(sucursalId)
                .precioCosto(new BigDecimal("7000.00"))
                .precioVenta(new BigDecimal("12500.00"))
                .activo(true)
                .build();
        given(ordenRepository.findByIdAndTallerId(ordenId, tallerId)).willReturn(Optional.of(orden));
        given(productoRepository.findById(productoId)).willReturn(Optional.of(producto));
        given(ordenProductoRepository.saveAll(anyList())).willAnswer(invocation -> {
            List<OrdenProducto> saved = invocation.getArgument(0);
            saved.getFirst().setId(lineId);
            return saved;
        });
        given(ordenProductoRepository.findResultByIdIn(List.of(lineId))).willReturn(List.of(
                new OrdenProductoResult(
                        lineId,
                        productoId,
                        "Pastillas",
                        "SKU-001",
                        2,
                        new BigDecimal("12500.00"),
                        new BigDecimal("12500.00"),
                        "Cliente trajo repuesto",
                        true
                )
        ));

        List<OrdenProductoResult> result = ordenService.agregarProductos(ordenId, List.of(
                new OrdenProductoAddCommand(productoId, 2, true, "Cliente trajo repuesto")
        ));

        assertThat(result).containsExactly(
                new OrdenProductoResult(
                        lineId,
                        productoId,
                        "Pastillas",
                        "SKU-001",
                        2,
                        new BigDecimal("12500.00"),
                        new BigDecimal("12500.00"),
                        "Cliente trajo repuesto",
                        true
                )
        );
        ArgumentCaptor<List<OrdenProducto>> captor = ArgumentCaptor.forClass(List.class);
        verify(ordenProductoRepository).saveAll(captor.capture());
        OrdenProducto saved = captor.getValue().getFirst();
        assertThat(saved.getOrdenId()).isEqualTo(ordenId);
        assertThat(saved.getProductoId()).isEqualTo(productoId);
        assertThat(saved.getCantidad()).isEqualTo(2);
        assertThat(saved.getPrecioCostoSnapshot()).isEqualByComparingTo("7000.00");
        assertThat(saved.getPrecioVentaSnapshot()).isEqualByComparingTo("12500.00");
        assertThat(saved.getPrecioAplicado()).isEqualByComparingTo("12500.00");
        assertThat(saved.getProporcionadoPorCliente()).isTrue();
        assertThat(saved.getNotas()).isEqualTo("Cliente trajo repuesto");
        assertThat(saved.getCreatedAt()).isNotNull();
        verify(ordenHistorialService).registrar(eq(ordenId), eq(AccionHistorialEnum.PRODUCTO_AGREGADO), eq("producto"), eq(productoId), any());
    }

    @Test
    void agregarServiciosUsesSucursalPriceOverride() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Orden orden = Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId).build();
        Servicio servicio = Servicio.builder()
                .id(servicioId)
                .tallerId(tallerId)
                .precioBase(new BigDecimal("8500.00"))
                .activo(true)
                .build();
        SucursalServicio override = SucursalServicio.builder()
                .sucursalId(sucursalId)
                .servicioId(servicioId)
                .precioPersonalizado(new BigDecimal("9900.00"))
                .build();
        given(ordenRepository.findByIdAndTallerId(ordenId, tallerId)).willReturn(Optional.of(orden));
        given(ordenServicioRepository.findByOrdenIdAndServicioId(ordenId, servicioId)).willReturn(Optional.empty());
        given(servicioRepository.findById(servicioId)).willReturn(Optional.of(servicio));
        given(sucursalServicioRepository.findBySucursalIdAndServicioId(sucursalId, servicioId))
                .willReturn(Optional.of(override));
        given(ordenServicioRepository.saveAll(anyList())).willAnswer(invocation -> {
            List<OrdenServicio> saved = invocation.getArgument(0);
            saved.getFirst().setId(lineId);
            return saved;
        });
        given(ordenServicioRepository.findResultByIdIn(List.of(lineId))).willReturn(List.of(
                new OrdenServicioResult(
                        lineId,
                        servicioId,
                        "Ajuste de frenos",
                        new BigDecimal("9900.00"),
                        new BigDecimal("9900.00"),
                        BigDecimal.ZERO,
                        "Ajustar delantero"
                )
        ));

        List<OrdenServicioResult> result = ordenService.agregarServicios(ordenId, List.of(
                new OrdenServicioAddCommand(servicioId, "Ajustar delantero")
        ));

        assertThat(result).containsExactly(
                new OrdenServicioResult(
                        lineId,
                        servicioId,
                        "Ajuste de frenos",
                        new BigDecimal("9900.00"),
                        new BigDecimal("9900.00"),
                        BigDecimal.ZERO,
                        "Ajustar delantero"
                )
        );
        ArgumentCaptor<List<OrdenServicio>> captor = ArgumentCaptor.forClass(List.class);
        verify(ordenServicioRepository).saveAll(captor.capture());
        OrdenServicio saved = captor.getValue().getFirst();
        assertThat(saved.getOrdenId()).isEqualTo(ordenId);
        assertThat(saved.getServicioId()).isEqualTo(servicioId);
        assertThat(saved.getPrecioBaseSnapshot()).isEqualByComparingTo("9900.00");
        assertThat(saved.getPrecioAplicado()).isEqualByComparingTo("9900.00");
        assertThat(saved.getDescuentoAplicado()).isEqualByComparingTo("0.00");
        assertThat(saved.getNotas()).isEqualTo("Ajustar delantero");
        assertThat(saved.getCreatedAt()).isNotNull();
        verify(ordenHistorialService).registrar(eq(ordenId), eq(AccionHistorialEnum.SERVICIO_AGREGADO), eq("servicio"), eq(servicioId), any());
    }

    @Test
    void agregarProductosRejectsProductFromDifferentSucursal() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(ordenRepository.findByIdAndTallerId(ordenId, tallerId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId).build()));
        given(productoRepository.findById(productoId))
                .willReturn(Optional.of(Producto.builder()
                        .id(productoId)
                        .sucursalId(UUID.randomUUID())
                        .activo(true)
                        .build()));

        assertThatThrownBy(() -> ordenService.agregarProductos(ordenId, List.of(
                new OrdenProductoAddCommand(productoId, 1, false, null)
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto " + productoId + " no pertenece a la sucursal de esta orden");

        verifyNoInteractions(ordenProductoRepository);
    }

    @Test
    void agregarProductosRejectsInactiveProduct() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(ordenRepository.findByIdAndTallerId(ordenId, tallerId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId).build()));
        given(productoRepository.findById(productoId))
                .willReturn(Optional.of(Producto.builder()
                        .id(productoId)
                        .sucursalId(sucursalId)
                        .activo(false)
                        .build()));

        assertThatThrownBy(() -> ordenService.agregarProductos(ordenId, List.of(
                new OrdenProductoAddCommand(productoId, 1, false, null)
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto no encontrado: " + productoId);

        verifyNoInteractions(ordenProductoRepository);
    }

    @Test
    void prepararMultimediaGeneratesOrderScopedKeyAndPresignedUrl() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).sucursalId(sucursalId).build()));
        given(r2Storage.presignPut(any(String.class), any(String.class), any(Duration.class)))
                .willReturn(new R2StoragePort.PresignedUpload("https://r2.example/presigned"));

        var result = ordenService.prepararMultimedia(ordenId.toString(), "image/jpeg");

        assertThat(result.presignedUrl()).isEqualTo("https://r2.example/presigned");
        assertThat(result.objectKey()).startsWith("ordenes/" + ordenId + "/").endsWith(".jpg");
        assertThat(result.publicUrl()).isEqualTo("https://media.example/" + result.objectKey());
        verify(r2Storage).presignPut(result.objectKey(), "image/jpeg", Duration.ofMinutes(15));
    }

    @Test
    void confirmarMultimediaVerifiesR2MetadataAndPersistsCurrentUser() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        String objectKey = "ordenes/" + ordenId + "/" + UUID.randomUUID() + ".jpg";
        String publicUrl = "https://media.example/" + objectKey;
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(usuarioId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).sucursalId(sucursalId).build()));
        given(r2Storage.head(objectKey))
                .willReturn(Optional.of(new R2StoragePort.ObjectMetadata("image/jpeg", 1024)));
        given(multimediaRepository.findByObjectKey(objectKey)).willReturn(Optional.empty());
        given(multimediaRepository.save(any(Multimedia.class))).willAnswer(invocation -> {
            Multimedia multimedia = invocation.getArgument(0);
            multimedia.setId(mediaId);
            return multimedia;
        });
        MultimediaResult persisted = new MultimediaResult(
                mediaId, usuarioId, "Rodrigo Soto", "image/jpeg", "imagen",
                publicUrl, "diagnostico", "Desgaste", OffsetDateTime.now()
        );
        given(multimediaRepository.findResultById(mediaId)).willReturn(Optional.of(persisted));

        var result = ordenService.confirmarMultimedia(
                ordenId.toString(), objectKey, publicUrl, "image/jpeg", "Desgaste", "diagnostico"
        );

        assertThat(result.created()).isTrue();
        assertThat(result.multimedia()).isEqualTo(persisted);
        ArgumentCaptor<Multimedia> captor = ArgumentCaptor.forClass(Multimedia.class);
        verify(multimediaRepository).save(captor.capture());
        assertThat(captor.getValue().getOrdenId()).isEqualTo(ordenId);
        assertThat(captor.getValue().getUsuarioId()).isEqualTo(usuarioId);
        assertThat(captor.getValue().getObjectKey()).isEqualTo(objectKey);
        assertThat(captor.getValue().getUrl()).isEqualTo(publicUrl);
    }

    @Test
    void confirmarMultimediaRejectsKeyFromAnotherOrderBeforeHead() {
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        String foreignKey = "ordenes/" + UUID.randomUUID() + "/" + UUID.randomUUID() + ".jpg";
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).sucursalId(sucursalId).build()));

        assertThatThrownBy(() -> ordenService.confirmarMultimedia(
                ordenId.toString(),
                foreignKey,
                "https://media.example/" + foreignKey,
                "image/jpeg",
                null,
                null
        ))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("objectKey invalido para la orden");

        verifyNoInteractions(r2Storage);
        verifyNoInteractions(multimediaRepository);
    }

    private OrdenCreateCommand baseCommand(UUID clienteId, UUID bicicletaId, UUID sucursalId) {
        return OrdenCreateCommand.builder()
                .clienteId(clienteId)
                .bicicletaId(bicicletaId)
                .sucursalId(sucursalId)
                .tipoTrabajo(UUID.fromString("31000000-0000-4000-8000-000000000002"))
                .prioridad(PrioridadOrdenEnum.media)
                .build();
    }

    private void stubDetalle(UUID ordenId, UUID sucursalId, UUID estadoId, UUID tipoId, UUID mecanicoId, String prioridad) {
        given(ordenRepository.findDetalleBaseByIdAndSucursalId(ordenId, sucursalId))
                .willReturn(Optional.of(new OrdenDetalleBaseResult(
                        ordenId,
                        "OT-000001",
                        UUID.randomUUID(),
                        sucursalId,
                        estadoId,
                        "esperando_repuestos",
                        "Esperando repuestos",
                        tipoId,
                        "revision",
                        "Revision",
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
                        null,
                        null,
                        null,
                        null,
                        UUID.randomUUID(),
                        "Cliente",
                        "Demo",
                        "+569",
                        "cliente@example.com",
                        "11.111.111-1",
                        mecanicoId,
                        mecanicoId != null ? "Diego" : null,
                        mecanicoId != null ? "Pizarro" : null,
                        prioridad
                )));
        given(comentarioRepository.findResultByOrdenId(ordenId)).willReturn(List.of());
        given(multimediaRepository.findResultByOrdenId(ordenId)).willReturn(List.of());
        given(ordenProductoRepository.findResultByOrdenId(ordenId)).willReturn(List.of());
        given(ordenServicioRepository.findResultByOrdenId(ordenId)).willReturn(List.of());
    }
}
