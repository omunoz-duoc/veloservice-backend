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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        given(clienteRepository.findCodigosClienteByTallerId(tallerId)).willReturn(List.of("CL-0001"));
        given(clienteRepository.existsByTallerIdAndCodigoCliente(tallerId, "CL-0002")).willReturn(false);
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
        assertThat(captor.getValue().getCodigoCliente()).isEqualTo("CL-0002");
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
                .codigoCliente("CL-0007")
                .tallerId(tallerId)
                .nombre("Ana")
                .apellido("Perez")
                .rut("11.111.111-1")
                .build();
        given(clienteRepository.findByTallerIdAndRut(tallerId, "11.111.111-1")).willReturn(Optional.of(existente));

        var result = clienteService.crear(new ClienteCreateCommand(
                "Ana", "Perez", "11.111.111-1", "999999999", "ana@test.cl", "Calle 1"));

        assertThat(result.getId()).isEqualTo(clienteId);
        assertThat(result.getCodigoCliente()).isEqualTo("CL-0007");
    }

    @Test
    void actualizarPermiteClienteDelTallerActual() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .codigoCliente("CL-0003")
                .tallerId(tallerId)
                .nombre("Ana")
                .apellido("Perez")
                .rut("11.111.111-1")
                .telefono("999999999")
                .email("ana@test.cl")
                .direccion("Calle 1")
                .build();
        given(clienteRepository.findByIdAndTallerId(clienteId, tallerId)).willReturn(Optional.of(cliente));
        given(clienteRepository.save(any(Cliente.class))).willAnswer(invocation -> invocation.getArgument(0));

        var result = clienteService.actualizar(clienteId, new ClienteCreateCommand(
                "Paula", "Soto", "22.222.222-2", "988888888", "paula@test.cl", "Calle 2"));

        assertThat(result.getId()).isEqualTo(clienteId);
        assertThat(result.getCodigoCliente()).isEqualTo("CL-0003");
        assertThat(result.getNombre()).isEqualTo("Paula");
        assertThat(result.getApellido()).isEqualTo("Soto");
        assertThat(result.getRut()).isEqualTo("22.222.222-2");
        assertThat(result.getTelefono()).isEqualTo("988888888");
        assertThat(result.getEmail()).isEqualTo("paula@test.cl");
        assertThat(result.getDireccion()).isEqualTo("Calle 2");
        assertThat(cliente.getUpdatedAt()).isNotNull();
        verify(clienteRepository).save(cliente);
    }

    @Test
    void actualizarRechazaClienteFueraDelTallerActual() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(clienteRepository.findByIdAndTallerId(clienteId, tallerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.actualizar(clienteId, new ClienteCreateCommand(
                "Paula", "Soto", "22.222.222-2", "988888888", "paula@test.cl", "Calle 2")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cliente no encontrado");
    }
}
