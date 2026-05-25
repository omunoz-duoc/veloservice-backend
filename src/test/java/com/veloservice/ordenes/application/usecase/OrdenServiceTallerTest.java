package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.TallerContext;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenEstadoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.ordenes.application.usecase.SecuenciaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTallerTest {

    @Mock private OrdenRepository ordenRepository;
    @Mock private OrdenEstadoRepository ordenEstadoRepository;
    @Mock private MultimediaRepository multimediaRepository;
    @Mock private ServicioRepository servicioRepository;
    @Mock private OrdenServicioRepository ordenServicioRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private OrdenProductoRepository ordenProductoRepository;
    @Mock private MovimientoStockRepository movimientoStockRepository;
    @Mock private SecuenciaService secuenciaService;
    @Mock private BicicletaRepository bicicletaRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private OrdenService ordenService;

    private final UUID tallerId = UUID.randomUUID();
    private final UUID sucursalId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ordenService = new OrdenService(
                ordenRepository,
                ordenEstadoRepository,
                multimediaRepository,
                servicioRepository,
                ordenServicioRepository,
                productoRepository,
                ordenProductoRepository,
                movimientoStockRepository,
                secuenciaService,
                bicicletaRepository,
                usuarioRepository
        );
    }

    @AfterEach
    void cleanup() {
        TallerContext.clear();
        SucursalContext.clear();
    }

    @Test
    void listarUrgentesUsesTallerRepoWhenTallerContextSet() {
        TallerContext.setCurrentTaller(tallerId);
        Orden urgente = Orden.builder()
                .sucursalId(sucursalId)
                .numeroOrden("U1")
                .estado(EstadoOrdenEnum.en_reparacion)
                .fechaPrometida(OffsetDateTime.now().minusDays(1))
                .build();
        given(ordenRepository.findAllByTallerIdOrderByFechaIngresoDesc(tallerId))
                .willReturn(List.of(urgente));

        List<?> result = ordenService.listarUrgentes();

        assertThat(result).hasSize(1);
        verify(ordenRepository).findAllByTallerIdOrderByFechaIngresoDesc(tallerId);
    }

    @Test
    void listarUrgentesFallsBackToSucursalRepoWhenNoTallerContext() {
        given(ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId))
                .willReturn(List.of());

        try (MockedStatic<SucursalContext> ctx = mockStatic(SucursalContext.class)) {
            ctx.when(SucursalContext::getCurrentSucursal).thenReturn(sucursalId);

            ordenService.listarUrgentes();
        }

        verify(ordenRepository).findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId);
    }

    @Test
    void listarUsesTallerRepoWhenTallerContextSet() {
        TallerContext.setCurrentTaller(tallerId);
        Orden orden = Orden.builder()
                .sucursalId(sucursalId)
                .numeroOrden("O1")
                .estado(EstadoOrdenEnum.recibida)
                .build();
        given(ordenRepository.findAllByTallerIdOrderByFechaIngresoDesc(tallerId))
                .willReturn(List.of(orden));

        List<?> result = ordenService.listar();

        assertThat(result).hasSize(1);
        verify(ordenRepository).findAllByTallerIdOrderByFechaIngresoDesc(tallerId);
    }

    @Test
    void metricasUsesTallerRepoWhenTallerContextSet() {
        TallerContext.setCurrentTaller(tallerId);
        Orden orden = Orden.builder()
                .sucursalId(sucursalId)
                .numeroOrden("M1")
                .estado(EstadoOrdenEnum.recibida)
                .build();
        given(ordenRepository.findAllByTallerIdOrderByFechaIngresoDesc(tallerId))
                .willReturn(List.of(orden));

        ordenService.metricas();

        verify(ordenRepository).findAllByTallerIdOrderByFechaIngresoDesc(tallerId);
    }
}
