package com.veloservice.servicios.application.usecase;

import com.veloservice.config.tenant.TallerContext;
import com.veloservice.servicios.application.dto.ServicioCreateCommand;
import com.veloservice.servicios.domain.model.Servicio;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.SucursalServicioRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock private ServicioRepository servicioRepository;
    @Mock private SucursalServicioRepository sucursalServicioRepository;
    @Mock private OrdenServicioRepository ordenServicioRepository;

    @AfterEach
    void cleanup() {
        TallerContext.clear();
    }

    @Test
    void listarOnlyUsesCurrentTallerServices() {
        UUID tallerId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        ServicioService service = new ServicioService(servicioRepository, sucursalServicioRepository);
        given(servicioRepository.findByTallerIdAndActivoTrueOrderByNombreAsc(tallerId))
                .willReturn(List.of(Servicio.builder()
                        .id(servicioId)
                        .tallerId(tallerId)
                        .nombre("Ajuste general")
                        .descripcion("Mantencion completa")
                        .precioBase(new BigDecimal("15000.00"))
                        .activo(true)
                        .build()));

        assertThat(service.listar()).singleElement()
                .satisfies(result -> {
                    assertThat(result.getId()).isEqualTo(servicioId);
                    assertThat(result.getNombre()).isEqualTo("Ajuste general");
                });

        verify(servicioRepository).findByTallerIdAndActivoTrueOrderByNombreAsc(tallerId);
        verify(servicioRepository, never()).findAll();
    }

    @Test
    void crearAsignaTallerDefaultsYCreatedAt() {
        UUID tallerId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        ServicioService service = new ServicioService(servicioRepository, sucursalServicioRepository, null, ordenServicioRepository);
        given(servicioRepository.save(any(Servicio.class))).willAnswer(invocation -> {
            Servicio servicio = invocation.getArgument(0);
            servicio.setId(servicioId);
            return servicio;
        });

        var result = service.crear(new ServicioCreateCommand(
                "Ajuste", "Ajuste general", new BigDecimal("12000.00"), null));

        assertThat(result.getId()).isEqualTo(servicioId);
        verify(servicioRepository).save(org.mockito.ArgumentMatchers.argThat(servicio ->
                tallerId.equals(servicio.getTallerId())
                        && Boolean.FALSE.equals(servicio.getEsGarantia())
                        && Boolean.TRUE.equals(servicio.getActivo())
                        && servicio.getCreatedAt() != null
        ));
    }

    @Test
    void actualizarBuscaServicioDelTallerActual() {
        UUID tallerId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        ServicioService service = new ServicioService(servicioRepository, sucursalServicioRepository, null, ordenServicioRepository);
        Servicio servicio = Servicio.builder()
                .id(servicioId)
                .tallerId(tallerId)
                .nombre("Antes")
                .descripcion("Antes")
                .precioBase(new BigDecimal("10000.00"))
                .activo(true)
                .build();
        given(servicioRepository.findByIdAndTallerId(servicioId, tallerId)).willReturn(Optional.of(servicio));
        given(servicioRepository.save(any(Servicio.class))).willAnswer(invocation -> invocation.getArgument(0));

        var result = service.actualizar(servicioId, new ServicioCreateCommand(
                "Despues", "Descripcion nueva", new BigDecimal("18000.00"), false));

        assertThat(result.getNombre()).isEqualTo("Despues");
        assertThat(result.getDescripcion()).isEqualTo("Descripcion nueva");
        assertThat(result.getPrecioBase()).isEqualByComparingTo("18000.00");
        assertThat(result.getActivo()).isFalse();
        verify(servicioRepository).findByIdAndTallerId(servicioId, tallerId);
        verify(servicioRepository, never()).findById(servicioId);
    }

    @Test
    void eliminarRechazaServicioConOrdenesAsociadas() {
        UUID tallerId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        ServicioService service = new ServicioService(servicioRepository, sucursalServicioRepository, null, ordenServicioRepository);
        Servicio servicio = Servicio.builder()
                .id(servicioId)
                .tallerId(tallerId)
                .build();
        given(servicioRepository.findByIdAndTallerId(servicioId, tallerId)).willReturn(Optional.of(servicio));
        given(ordenServicioRepository.existsByServicioId(servicioId)).willReturn(true);

        assertThatThrownBy(() -> service.eliminar(servicioId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede eliminar un servicio con órdenes asociadas.");
    }

    @Test
    void eliminarBorraServicioSinOrdenesAsociadas() {
        UUID tallerId = UUID.randomUUID();
        UUID servicioId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        ServicioService service = new ServicioService(servicioRepository, sucursalServicioRepository, null, ordenServicioRepository);
        Servicio servicio = Servicio.builder()
                .id(servicioId)
                .tallerId(tallerId)
                .build();
        given(servicioRepository.findByIdAndTallerId(servicioId, tallerId)).willReturn(Optional.of(servicio));
        given(ordenServicioRepository.existsByServicioId(servicioId)).willReturn(false);

        service.eliminar(servicioId);

        verify(servicioRepository).delete(servicio);
    }
}
