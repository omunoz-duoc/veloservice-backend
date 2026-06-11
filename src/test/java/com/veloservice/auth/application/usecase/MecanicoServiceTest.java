package com.veloservice.auth.application.usecase;

import com.veloservice.auth.domain.model.Rol;
import com.veloservice.auth.domain.model.Usuario;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MecanicoServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private OrdenRepository ordenRepository;

    @AfterEach
    void cleanup() {
        TallerContext.clear();
        SucursalContext.clear();
    }

    @Test
    void listarActivosFiltersBySucursalWhenContextHasSucursal() {
        UUID sucursalId = UUID.randomUUID();
        UUID mecanicoId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        MecanicoService service = new MecanicoService(usuarioRepository, ordenRepository);
        given(usuarioRepository.findActiveMecanicosBySucursalId(sucursalId))
                .willReturn(List.of(usuario(mecanicoId)));
        given(ordenRepository.countActivasByMecanicoIdAndSucursalId(eq(mecanicoId), eq(sucursalId), anyList()))
                .willReturn(4L);

        var result = service.listarActivos();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(mecanicoId);
        assertThat(result.getFirst().rol()).isEqualTo("mecanico");
        assertThat(result.getFirst().ordenesActivas()).isEqualTo(4L);
        verify(usuarioRepository).findActiveMecanicosBySucursalId(sucursalId);
        verify(ordenRepository).countActivasByMecanicoIdAndSucursalId(eq(mecanicoId), eq(sucursalId), anyList());
    }

    @Test
    void listarActivosFiltersByTallerWhenOnlyTallerContextExists() {
        UUID tallerId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        MecanicoService service = new MecanicoService(usuarioRepository, ordenRepository);
        given(usuarioRepository.findActiveMecanicosByTallerId(tallerId)).willReturn(List.of());

        var result = service.listarActivos();

        assertThat(result).isEmpty();
        verify(usuarioRepository).findActiveMecanicosByTallerId(tallerId);
    }

    @Test
    void listarActivosReturnsEmptyWithoutTenantContext() {
        MecanicoService service = new MecanicoService(usuarioRepository, ordenRepository);

        assertThat(service.listarActivos()).isEmpty();
        verifyNoInteractions(usuarioRepository, ordenRepository);
    }

    private Usuario usuario(UUID id) {
        return Usuario.builder()
                .id(id)
                .nombre("Diego")
                .apellido("Pizarro")
                .email("mecanico@andespedal.cl")
                .rol(Rol.builder().nombre("mecanico").build())
                .build();
    }
}
