package com.veloservice.servicios.application.usecase;

import com.veloservice.config.tenant.TallerContext;
import com.veloservice.servicios.domain.model.Servicio;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.SucursalServicioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServicioServiceTest {

    @Mock private ServicioRepository servicioRepository;
    @Mock private SucursalServicioRepository sucursalServicioRepository;

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
}
