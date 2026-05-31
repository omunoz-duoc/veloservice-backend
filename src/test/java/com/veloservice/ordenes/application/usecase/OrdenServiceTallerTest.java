package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrdenServiceTallerTest {

    @Mock private OrdenRepository ordenRepository;

    private OrdenService ordenService;

    @BeforeEach
    void setUp() {
        ordenService = new OrdenService(ordenRepository);
    }

    @AfterEach
    void cleanup() {
        TallerContext.clear();
        SucursalContext.clear();
    }

    @Test
    void listarUsesTallerScopeWhenTallerContextExists() {
        UUID tallerId = UUID.randomUUID();
        OrdenReadResult orden = orden("OT-1");
        TallerContext.setCurrentTaller(tallerId);
        given(ordenRepository.findReadByTallerId(tallerId)).willReturn(List.of(orden));

        List<OrdenReadResult> result = ordenService.listar();

        assertThat(result).containsExactly(orden);
        verify(ordenRepository).findReadByTallerId(tallerId);
    }

    @Test
    void listarFallsBackToSucursalScope() {
        UUID sucursalId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findReadBySucursalId(sucursalId)).willReturn(List.of());

        List<OrdenReadResult> result = ordenService.listar();

        assertThat(result).isEmpty();
        verify(ordenRepository).findReadBySucursalId(sucursalId);
    }

    @Test
    void listarPrefersSucursalScopeWhenBothContextsExist() {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findReadBySucursalId(sucursalId)).willReturn(List.of());

        List<OrdenReadResult> result = ordenService.listar();

        assertThat(result).isEmpty();
        verify(ordenRepository).findReadBySucursalId(sucursalId);
    }

    @Test
    void obtenerTriesUuidThenNumeroOrdenWithinTaller() {
        UUID tallerId = UUID.randomUUID();
        UUID ordenId = UUID.randomUUID();
        OrdenReadResult orden = orden("OT-2");
        TallerContext.setCurrentTaller(tallerId);
        given(ordenRepository.findReadByIdAndTallerId(ordenId, tallerId)).willReturn(Optional.empty());
        given(ordenRepository.findReadByNumeroOrdenAndTallerId(ordenId.toString(), tallerId))
                .willReturn(Optional.of(orden));

        OrdenReadResult result = ordenService.obtener(ordenId.toString());

        assertThat(result).isEqualTo(orden);
        verify(ordenRepository).findReadByIdAndTallerId(ordenId, tallerId);
        verify(ordenRepository).findReadByNumeroOrdenAndTallerId(ordenId.toString(), tallerId);
    }

    @Test
    void obtenerUsesSucursalScopeForNumeroOrden() {
        UUID sucursalId = UUID.randomUUID();
        OrdenReadResult orden = orden("OT-3");
        SucursalContext.setCurrentSucursal(sucursalId);
        given(ordenRepository.findReadByNumeroOrdenAndSucursalId("OT-3", sucursalId)).willReturn(Optional.of(orden));

        OrdenReadResult result = ordenService.obtener("OT-3");

        assertThat(result).isEqualTo(orden);
        verify(ordenRepository).findReadByNumeroOrdenAndSucursalId("OT-3", sucursalId);
    }

    @Test
    void listarRequiresTenantContext() {
        assertThatThrownBy(ordenService::listar)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Contexto de taller o sucursal requerido");
        verifyNoInteractions(ordenRepository);
    }

    private OrdenReadResult orden(String numeroOrden) {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        UUID estadoId = UUID.randomUUID();
        UUID tipoId = UUID.randomUUID();
        UUID bicicletaId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        return new OrdenReadResult(
                UUID.randomUUID(),
                numeroOrden,
                tallerId,
                sucursalId,
                estadoId,
                "recibida",
                "Recibida",
                tipoId,
                "reparacion",
                "Reparacion",
                OffsetDateTime.now(),
                null,
                null,
                "Diagnostico",
                null,
                null,
                bicicletaId,
                "Trek",
                "Marlin",
                "MTB",
                "29",
                "Negro",
                "SN-1",
                clienteId,
                "Ana",
                "Perez",
                "+569",
                "ana@example.com",
                "11111111-1",
                null,
                null,
                null
        );
    }
}
