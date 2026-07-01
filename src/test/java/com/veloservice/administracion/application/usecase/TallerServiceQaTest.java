package com.veloservice.administracion.application.usecase;

import com.veloservice.administracion.application.dto.NuevoTallerCommand;
import com.veloservice.administracion.domain.model.Taller;
import com.veloservice.administracion.infraestructure.persistence.repository.TallerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TallerServiceQaTest {

    @Mock private TallerRepository tallerRepository;
    @InjectMocks private TallerService tallerService;

    @Test
    void cp028_crearNuevoTenantComoAdminSaas_debePersistirTallerActivo() {
        UUID tallerId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        NuevoTallerCommand command = new NuevoTallerCommand(
                planId, "Velo Centro", "76.123.456-7", "+56911111111", "admin@velo.cl", null);
        given(tallerRepository.findByRut(command.getRut())).willReturn(Optional.empty());
        given(tallerRepository.save(any(Taller.class))).willAnswer(invocation -> {
            Taller taller = invocation.getArgument(0);
            taller.setId(tallerId);
            return taller;
        });

        var result = tallerService.crear(command);

        assertThat(result.getId()).isEqualTo(tallerId);
        assertThat(result.getPlanId()).isEqualTo(planId);
        assertThat(result.isActivo()).isTrue();
        ArgumentCaptor<Taller> captor = ArgumentCaptor.forClass(Taller.class);
        verify(tallerRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void cp029_crearTenantConRutDuplicado_debeLanzarBadRequestSinGuardar() {
        UUID planId = UUID.randomUUID();
        NuevoTallerCommand command = new NuevoTallerCommand(
                planId, "Duplicado", "76.123.456-7", null, null, null);
        given(tallerRepository.findByRut(command.getRut()))
                .willReturn(Optional.of(Taller.builder().id(UUID.randomUUID()).rut(command.getRut()).build()));

        assertThatThrownBy(() -> tallerService.crear(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un taller");
        verify(tallerRepository, never()).save(any());
    }
}
