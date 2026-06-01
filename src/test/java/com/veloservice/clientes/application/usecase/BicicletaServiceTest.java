package com.veloservice.clientes.application.usecase;

import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.config.tenant.TallerContext;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BicicletaServiceTest {

    @Mock private BicicletaRepository bicicletaRepository;
    @Mock private ClienteRepository clienteRepository;

    @InjectMocks
    private BicicletaService bicicletaService;

    @AfterEach
    void cleanup() {
        TallerContext.clear();
    }

    @Test
    void crearPermiteClienteDelTallerActual() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(clienteRepository.findByIdAndTallerId(clienteId, tallerId))
                .willReturn(Optional.of(Cliente.builder().id(clienteId).tallerId(tallerId).build()));
        given(bicicletaRepository.save(any(Bicicleta.class))).willAnswer(invocation -> {
            Bicicleta bicicleta = invocation.getArgument(0);
            bicicleta.setId(UUID.randomUUID());
            return bicicleta;
        });

        bicicletaService.crear(clienteId, new BicicletaCreateCommand(
                "Trek", "Marlin", "MTB", "29", "Azul", "ABC123", 2024, "Notas"));

        ArgumentCaptor<Bicicleta> captor = ArgumentCaptor.forClass(Bicicleta.class);
        verify(bicicletaRepository).save(captor.capture());
        assertThat(captor.getValue().getClienteId()).isEqualTo(clienteId);
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void crearRechazaClienteFueraDelTallerActual() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(clienteRepository.findByIdAndTallerId(clienteId, tallerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> bicicletaService.crear(clienteId, new BicicletaCreateCommand(
                "Trek", "Marlin", "MTB", "29", "Azul", "ABC123", 2024, "Notas")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cliente no encontrado");
    }
}
