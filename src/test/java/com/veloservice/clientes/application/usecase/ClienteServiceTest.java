package com.veloservice.clientes.application.usecase;

import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.MembresiaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private MembresiaRepository membresiaRepository;

    @InjectMocks
    private ClienteService clienteService;

    @AfterEach
    void cleanup() {
        TallerContext.clear();
    }

    @Test
    void crearScopesClienteToCurrentTaller() {
        UUID tallerId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(clienteRepository.findByTallerIdAndRut(tallerId, "11.111.111-1")).willReturn(Optional.empty());
        given(clienteRepository.save(any(Cliente.class))).willAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(UUID.randomUUID());
            return cliente;
        });

        clienteService.crear(new ClienteCreateCommand(
                "Ana", "Perez", "11.111.111-1", "999999999", "ana@test.cl", "Calle 1"));

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getTallerId()).isEqualTo(tallerId);
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void crearReusesRutOnlyWithinSameTaller() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Cliente existente = Cliente.builder()
                .id(clienteId)
                .tallerId(tallerId)
                .nombre("Ana")
                .apellido("Perez")
                .rut("11.111.111-1")
                .build();
        given(clienteRepository.findByTallerIdAndRut(tallerId, "11.111.111-1")).willReturn(Optional.of(existente));

        var result = clienteService.crear(new ClienteCreateCommand(
                "Ana", "Perez", "11.111.111-1", "999999999", "ana@test.cl", "Calle 1"));

        assertThat(result.getId()).isEqualTo(clienteId);
    }
}
