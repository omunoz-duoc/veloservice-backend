package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.clientes.application.usecase.BicicletaService;
import com.veloservice.clientes.application.usecase.ClienteService;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.domain.PrioridadOrdenEnum;
import com.veloservice.ordenes.domain.model.EstadoOrden;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenEstado;
import com.veloservice.ordenes.domain.model.OrdenProducto;
import com.veloservice.ordenes.domain.model.OrdenServicio;
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
import com.veloservice.shared.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
                sucursalRepository
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
        given(tipoOrdenRepository.findByCodigo("mantencion"))
                .willReturn(Optional.of(TipoOrden.builder().id(UUID.randomUUID()).codigo("mantencion").build()));
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
    }

    @Test
    void cambiarEstadoRequiresUsuarioContext() {
        SucursalContext.setCurrentSucursal(UUID.randomUUID());

        assertThatThrownBy(() -> ordenService.cambiarEstado("OT-000001", new OrdenEstadoChangeCommand("en_diagnostico", null)))
                .isInstanceOf(IllegalStateException.class)
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
                .build();
        given(ordenRepository.findById(ordenId)).willReturn(Optional.of(orden));
        given(productoRepository.findById(productoId)).willReturn(Optional.of(producto));
        given(ordenProductoRepository.saveAll(anyList())).willAnswer(invocation -> {
            List<OrdenProducto> saved = invocation.getArgument(0);
            saved.getFirst().setId(lineId);
            return saved;
        });
        given(ordenProductoRepository.findResultByIdIn(List.of(lineId))).willReturn(List.of(
                new OrdenProductoResult(lineId, productoId, "Pastillas", "SKU-001", 2, new BigDecimal("12500.00"))
        ));

        List<OrdenProductoResult> result = ordenService.agregarProductos(ordenId, List.of(
                new OrdenProductoAddCommand(productoId, 2, true, "Cliente trajo repuesto")
        ));

        assertThat(result).containsExactly(
                new OrdenProductoResult(lineId, productoId, "Pastillas", "SKU-001", 2, new BigDecimal("12500.00"))
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
                .build();
        SucursalServicio override = SucursalServicio.builder()
                .sucursalId(sucursalId)
                .servicioId(servicioId)
                .precioPersonalizado(new BigDecimal("9900.00"))
                .build();
        given(ordenRepository.findById(ordenId)).willReturn(Optional.of(orden));
        given(servicioRepository.findById(servicioId)).willReturn(Optional.of(servicio));
        given(sucursalServicioRepository.findBySucursalIdAndServicioId(sucursalId, servicioId))
                .willReturn(Optional.of(override));
        given(ordenServicioRepository.saveAll(anyList())).willAnswer(invocation -> {
            List<OrdenServicio> saved = invocation.getArgument(0);
            saved.getFirst().setId(lineId);
            return saved;
        });
        given(ordenServicioRepository.findResultByIdIn(List.of(lineId))).willReturn(List.of(
                new OrdenServicioResult(lineId, servicioId, "Ajuste de frenos", new BigDecimal("9900.00"))
        ));

        List<OrdenServicioResult> result = ordenService.agregarServicios(ordenId, List.of(
                new OrdenServicioAddCommand(servicioId, "Ajustar delantero")
        ));

        assertThat(result).containsExactly(
                new OrdenServicioResult(lineId, servicioId, "Ajuste de frenos", new BigDecimal("9900.00"))
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
    }

    @Test
    void agregarProductosRejectsProductFromDifferentSucursal() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(ordenRepository.findById(ordenId))
                .willReturn(Optional.of(Orden.builder().id(ordenId).tallerId(tallerId).sucursalId(sucursalId).build()));
        given(productoRepository.findById(productoId))
                .willReturn(Optional.of(Producto.builder().id(productoId).sucursalId(UUID.randomUUID()).build()));

        assertThatThrownBy(() -> ordenService.agregarProductos(ordenId, List.of(
                new OrdenProductoAddCommand(productoId, 1, false, null)
        )))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Producto " + productoId + " no pertenece a la sucursal de esta orden");

        verifyNoInteractions(ordenProductoRepository);
    }

    private OrdenCreateCommand baseCommand(UUID clienteId, UUID bicicletaId, UUID sucursalId) {
        return OrdenCreateCommand.builder()
                .clienteId(clienteId)
                .bicicletaId(bicicletaId)
                .sucursalId(sucursalId)
                .tipoTrabajo("mantencion")
                .prioridad(PrioridadOrdenEnum.media)
                .build();
    }
}
