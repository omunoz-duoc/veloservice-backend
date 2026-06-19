package com.veloservice.finanzas.application.usecase;

import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.MembresiaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.SucursalClienteRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.finanzas.application.dto.CobroCreateCommand;
import com.veloservice.finanzas.application.dto.CobroResult;
import com.veloservice.finanzas.domain.MetodoPagoEnum;
import com.veloservice.finanzas.domain.TipoDocumentoEnum;
import com.veloservice.finanzas.domain.model.Cobro;
import com.veloservice.finanzas.infraestructure.persistence.repository.CobroRepository;
import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenProducto;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CobroServiceTest {

    @Mock private CobroRepository cobroRepository;
    @Mock private OrdenRepository ordenRepository;
    @Mock private OrdenServicioRepository ordenServicioRepository;
    @Mock private OrdenProductoRepository ordenProductoRepository;
    @Mock private BicicletaRepository bicicletaRepository;
    @Mock private SucursalClienteRepository sucursalClienteRepository;
    @Mock private MembresiaRepository membresiaRepository;
    @Mock private SucursalRepository sucursalRepository;

    private CobroService cobroService;

    @BeforeEach
    void setUp() {
        cobroService = new CobroService(
                cobroRepository,
                ordenRepository,
                ordenServicioRepository,
                ordenProductoRepository,
                bicicletaRepository,
                sucursalClienteRepository,
                membresiaRepository,
                sucursalRepository
        );
    }

    @AfterEach
    void cleanup() {
        SucursalContext.clear();
        UsuarioContext.clear();
    }

    @Test
    void liquidarCalculatesProductSubtotalUsingQuantity() {
        UUID sucursalId = UUID.randomUUID();
        UUID usuarioId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        UUID bicicletaId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        UsuarioContext.setCurrentUser(usuarioId);
        Orden orden = Orden.builder()
                .id(ordenId)
                .sucursalId(sucursalId)
                .bicicletaId(bicicletaId)
                .build();
        orden.setEstado(EstadoOrdenEnum.entregada);
        OrdenProducto producto = OrdenProducto.builder()
                .ordenId(ordenId)
                .cantidad(3)
                .precioAplicado(new BigDecimal("1000.00"))
                .build();
        given(ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)).willReturn(Optional.of(orden));
        given(cobroRepository.findByOrdenId(ordenId)).willReturn(Optional.empty());
        given(ordenServicioRepository.findByOrdenId(ordenId)).willReturn(List.of());
        given(ordenProductoRepository.findByOrdenId(ordenId)).willReturn(List.of(producto));
        given(bicicletaRepository.findById(bicicletaId)).willReturn(Optional.empty());
        given(cobroRepository.save(any(Cobro.class))).willAnswer(invocation -> {
            Cobro cobro = invocation.getArgument(0);
            cobro.setId(UUID.randomUUID());
            return cobro;
        });

        CobroResult result = cobroService.liquidar(new CobroCreateCommand(
                ordenId,
                TipoDocumentoEnum.boleta,
                "1",
                MetodoPagoEnum.efectivo,
                BigDecimal.ZERO
        ));

        assertThat(result.getSubtotalProductos()).isEqualByComparingTo("3000.00");
        assertThat(result.getNeto()).isEqualByComparingTo("3000.00");
        assertThat(result.getIva()).isEqualByComparingTo("570.00");
        assertThat(result.getTotal()).isEqualByComparingTo("3570.00");
    }
}
