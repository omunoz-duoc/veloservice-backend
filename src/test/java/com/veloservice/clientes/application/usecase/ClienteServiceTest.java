package com.veloservice.clientes.application.usecase;

import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.MembresiaRepository;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.clientes.application.dto.OrdenResumenResult;
import com.veloservice.ordenes.application.dto.OrdenResumenClienteResult;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.shared.application.exception.ConflictException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private MembresiaRepository membresiaRepository;
    @Mock private OrdenRepository ordenRepository;

    @InjectMocks
    private ClienteService clienteService;

    @AfterEach
    void cleanup() {
        TallerContext.clear();
    }

    @Test
    void crearScopesClienteToCurrentTallerAndNormalizesRut() {
        UUID tallerId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(clienteRepository.findByTallerIdAndRut(tallerId, "182950907")).willReturn(Optional.empty());
        given(clienteRepository.save(any(Cliente.class))).willAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(UUID.randomUUID());
            return cliente;
        });

        clienteService.crear(new ClienteCreateCommand(
                "Ana", "Perez", "18.295.090-7", "999999999", "ana@test.cl", "Calle 1"));

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getTallerId()).isEqualTo(tallerId);
        assertThat(captor.getValue().getRut()).isEqualTo("182950907");
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void crearRechazaRutDuplicadoEnElMismoTaller() {
        UUID tallerId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Cliente existente = Cliente.builder()
                .id(UUID.randomUUID())
                .tallerId(tallerId)
                .nombre("Ana")
                .apellido("Perez")
                .rut("182950907")
                .build();
        // RUT comes formatted from the front end but matches the normalized stored value.
        given(clienteRepository.findByTallerIdAndRut(tallerId, "182950907")).willReturn(Optional.of(existente));

        assertThatThrownBy(() -> clienteService.crear(new ClienteCreateCommand(
                "Ana", "Perez", "18.295.090-7", "999999999", "ana@test.cl", "Calle 1")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("182950907");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void actualizarPermiteClienteDelTallerActual() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Cliente cliente = Cliente.builder()
                .id(clienteId)
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
        assertThat(result.getNombre()).isEqualTo("Paula");
        assertThat(result.getApellido()).isEqualTo("Soto");
        assertThat(result.getRut()).isEqualTo("222222222");
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

    @Test
    void obtenerDetalleReturnsFullProfileWithBikesAndOrders() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        OffsetDateTime now = OffsetDateTime.now();

        Bicicleta b1 = Bicicleta.builder()
                .id(UUID.randomUUID())
                .marca("Trek")
                .modelo("Marlin 5")
                .tipo("MTB")
                .aro("29")
                .color("Rojo")
                .numeroSerie("ABC123")
                .anio(2023)
                .notas("Nota 1")
                .build();
        Bicicleta b2 = Bicicleta.builder()
                .id(UUID.randomUUID())
                .marca("Giant")
                .modelo("Talon")
                .tipo("MTB")
                .aro("27.5")
                .color("Azul")
                .numeroSerie("DEF456")
                .anio(2022)
                .notas("Nota 2")
                .build();

        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .tallerId(tallerId)
                .nombre("Juan")
                .apellido("Pérez")
                .email("juan@example.com")
                .telefono("+56912345678")
                .direccion("Av. Siempre Viva 123")
                .rut("12345678-9")
                .createdAt(now)
                .bicicletas(List.of(b1, b2))
                .build();

        given(clienteRepository.findByIdAndTallerId(clienteId, tallerId)).willReturn(Optional.of(cliente));
        given(ordenRepository.countByClienteIdAndTallerId(clienteId, tallerId)).willReturn(7L);
        given(ordenRepository.findResumenByClienteIdAndTallerId(clienteId, tallerId, org.springframework.data.domain.Pageable.ofSize(5)))
                .willReturn(List.of(
                        new OrdenResumenClienteResult("OT-0001", "Mantención", "En proceso", now.minusDays(1)),
                        new OrdenResumenClienteResult("OT-0002", "Reparación", "Completada", now.minusDays(5))
                ));

        var result = clienteService.obtenerDetalle(clienteId);

        assertThat(result.getNombre()).isEqualTo("Juan");
        assertThat(result.getApellido()).isEqualTo("Pérez");
        assertThat(result.getEmail()).isEqualTo("juan@example.com");
        assertThat(result.getTelefono()).isEqualTo("+56912345678");
        assertThat(result.getDireccion()).isEqualTo("Av. Siempre Viva 123");
        assertThat(result.getRut()).isEqualTo("12345678-9");
        assertThat(result.getClienteDesde()).isEqualTo(now);
        assertThat(result.getBicicletasCount()).isEqualTo(2);
        assertThat(result.getBicicletas()).hasSize(2)
                .anySatisfy(b -> assertThat(b.numeroSerie()).isEqualTo("ABC123"))
                .anySatisfy(b -> assertThat(b.numeroSerie()).isEqualTo("DEF456"));
        assertThat(result.getOtsCount()).isEqualTo(7L);
        assertThat(result.getLastOts()).hasSize(2)
                .extracting(OrdenResumenResult::numeroOrden)
                .containsExactly("OT-0001", "OT-0002");

        verify(ordenRepository).countByClienteIdAndTallerId(clienteId, tallerId);
        verify(ordenRepository).findResumenByClienteIdAndTallerId(clienteId, tallerId, org.springframework.data.domain.Pageable.ofSize(5));
    }

    @Test
    void obtenerDetalleReturnsEmptyForZeroBikeClient() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        OffsetDateTime now = OffsetDateTime.now();

        Cliente cliente = Cliente.builder()
                .id(clienteId)
                .tallerId(tallerId)
                .nombre("Ana")
                .apellido("Soto")
                .email("ana@example.com")
                .createdAt(now)
                .bicicletas(List.of())
                .build();

        given(clienteRepository.findByIdAndTallerId(clienteId, tallerId)).willReturn(Optional.of(cliente));
        given(ordenRepository.countByClienteIdAndTallerId(clienteId, tallerId)).willReturn(0L);
        given(ordenRepository.findResumenByClienteIdAndTallerId(clienteId, tallerId, org.springframework.data.domain.Pageable.ofSize(5)))
                .willReturn(List.of());

        var result = clienteService.obtenerDetalle(clienteId);

        assertThat(result.getBicicletasCount()).isEqualTo(0);
        assertThat(result.getBicicletas()).isEmpty();
        assertThat(result.getOtsCount()).isEqualTo(0L);
        assertThat(result.getLastOts()).isEmpty();
    }

    @Test
    void obtenerDetalleThrowsWhenClienteNotFound() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(clienteRepository.findByIdAndTallerId(clienteId, tallerId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.obtenerDetalle(clienteId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cliente no encontrado");
    }

    @Test
    void obtenerDetalleThrowsWhenTallerContextIsNull() {
        assertThatThrownBy(() -> clienteService.obtenerDetalle(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Operacion requiere contexto de taller");
    }
}
