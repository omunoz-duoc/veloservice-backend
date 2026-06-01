package com.veloservice.ordenes.application.usecase;

import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SecuenciaServiceTest {

    @Mock
    private OrdenRepository ordenRepository;

    @InjectMocks
    private SecuenciaService secuenciaService;

    @Test
    void generarNumeroOrdenUsesHighestExistingOtNumber() {
        UUID tallerId = UUID.randomUUID();
        given(ordenRepository.findNumerosOrdenByTallerId(tallerId))
                .willReturn(List.of("OT-00001", "OT-00004", "OT-00002"));
        given(ordenRepository.existsByNumeroOrdenAndTallerId("OT-00005", tallerId)).willReturn(false);

        assertThat(secuenciaService.generarNumeroOrden(tallerId)).isEqualTo("OT-00005");
    }

    @Test
    void generarNumeroOrdenIgnoresLegacyMockDataFormat() {
        UUID tallerId = UUID.randomUUID();
        given(ordenRepository.findNumerosOrdenByTallerId(tallerId))
                .willReturn(List.of("AP-2026-0001", "AP-2026-0004"));
        given(ordenRepository.existsByNumeroOrdenAndTallerId("OT-00001", tallerId)).willReturn(false);

        assertThat(secuenciaService.generarNumeroOrden(tallerId)).isEqualTo("OT-00001");
    }

    @Test
    void generarNumeroOrdenSkipsUnexpectedExistingCollision() {
        UUID tallerId = UUID.randomUUID();
        given(ordenRepository.findNumerosOrdenByTallerId(tallerId))
                .willReturn(List.of("OT-00003"));
        given(ordenRepository.existsByNumeroOrdenAndTallerId("OT-00004", tallerId)).willReturn(true);
        given(ordenRepository.existsByNumeroOrdenAndTallerId("OT-00005", tallerId)).willReturn(false);

        assertThat(secuenciaService.generarNumeroOrden(tallerId)).isEqualTo("OT-00005");
    }
}
