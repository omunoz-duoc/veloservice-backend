package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrdenServiceEstadosTest {

    @Mock OrdenRepository ordenRepository;
    @Mock com.veloservice.ordenes.infraestructure.persistence.repository.OrdenEstadoRepository ordenEstadoRepository;
    @Mock com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository multimediaRepository;
    @Mock com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository servicioRepository;
    @Mock com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository ordenServicioRepository;
    @Mock com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository productoRepository;
    @Mock com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository ordenProductoRepository;
    @Mock com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository movimientoStockRepository;
    @Mock com.veloservice.ordenes.application.usecase.SecuenciaService secuenciaService;
    @Mock com.veloservice.ordenes.application.port.BicicletaPort bicicletaPort;
    @Mock com.veloservice.ordenes.application.port.UsuarioPort usuarioPort;

    @InjectMocks OrdenService ordenService;

    private final UUID sucursalId = UUID.randomUUID();
    private final UUID mecanicoId = UUID.randomUUID();

    @BeforeEach
    void setContext() {
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(mecanicoId);
    }

    @AfterEach
    void clearContext() {
        SucursalContext.clear();
        UsuarioContext.clear();
    }

    @Test
    void contarPorEstado_agrupa_canceladas_excluidas() {
        Orden activa = new Orden();
        activa.setEstado(EstadoOrdenEnum.en_reparacion);
        Orden cancelada = new Orden();
        cancelada.setEstado(EstadoOrdenEnum.cancelada);

        when(ordenRepository.findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(sucursalId, mecanicoId))
                .thenReturn(List.of(activa, cancelada));

        Map<String, Long> resultado = ordenService.contarPorEstado();

        assertThat(resultado).containsKey("en_proceso");
        assertThat(resultado).doesNotContainKey("cancelada");
        assertThat(resultado.get("en_proceso")).isEqualTo(1L);
    }

    @Test
    void contarPorEstado_retorna_vacio_sin_contexto() {
        SucursalContext.clear();
        UsuarioContext.clear();

        Map<String, Long> resultado = ordenService.contarPorEstado();

        assertThat(resultado).isEmpty();
    }
}
