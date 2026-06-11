package com.veloservice.clientes.application.usecase;

import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
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
    @Mock private OrdenRepository ordenRepository;

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

    @Test
    void actualizarMantieneClienteYActualizaCampos() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID bicicletaId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Bicicleta bicicleta = Bicicleta.builder()
                .id(bicicletaId)
                .clienteId(clienteId)
                .marca("Trek")
                .modelo("Marlin")
                .tipo("MTB")
                .aro("29")
                .color("Azul")
                .numeroSerie("OLD123")
                .anio(2022)
                .notas("Anterior")
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now().minusDays(1))
                .build();
        BicicletaCreateCommand command = new BicicletaCreateCommand(
                "Specialized", "Rockhopper", "Urbana", "27.5", "Rojo", "NEW123", 2024, "Actualizada");
        given(bicicletaRepository.findByIdAndClienteTallerId(bicicletaId, tallerId))
                .willReturn(Optional.of(bicicleta));
        given(bicicletaRepository.save(any(Bicicleta.class))).willAnswer(invocation -> invocation.getArgument(0));

        var result = bicicletaService.actualizar(bicicletaId, command);

        assertThat(result.getId()).isEqualTo(bicicletaId);
        assertThat(result.getClienteId()).isEqualTo(clienteId);
        assertThat(result.getMarca()).isEqualTo("Specialized");
        assertThat(result.getModelo()).isEqualTo("Rockhopper");
        assertThat(result.getTipo()).isEqualTo("Urbana");
        assertThat(result.getAro()).isEqualTo("27.5");
        assertThat(result.getColor()).isEqualTo("Rojo");
        assertThat(result.getNumeroSerie()).isEqualTo("NEW123");
        assertThat(result.getAnio()).isEqualTo(2024);
        assertThat(result.getNotas()).isEqualTo("Actualizada");
        verify(bicicletaRepository).save(bicicleta);
    }

    @Test
    void actualizarRechazaBicicletaFueraDelTallerActual() {
        UUID tallerId = UUID.randomUUID();
        UUID bicicletaId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        given(bicicletaRepository.findByIdAndClienteTallerId(bicicletaId, tallerId))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> bicicletaService.actualizar(bicicletaId, new BicicletaCreateCommand(
                "Trek", "Marlin", "MTB", "29", "Azul", "ABC123", 2024, "Notas")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Bicicleta no encontrada");
    }

    @Test
    void eliminarBicicletaDelTallerActual() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID bicicletaId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Bicicleta bicicleta = Bicicleta.builder()
                .id(bicicletaId)
                .clienteId(clienteId)
                .build();
        given(bicicletaRepository.findByIdAndClienteTallerId(bicicletaId, tallerId))
                .willReturn(Optional.of(bicicleta));
        given(ordenRepository.existsByBicicletaId(bicicletaId)).willReturn(false);

        bicicletaService.eliminar(bicicletaId);

        verify(bicicletaRepository).delete(bicicleta);
    }

    @Test
    void eliminarRechazaBicicletaConOrdenesAsociadas() {
        UUID tallerId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID bicicletaId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Bicicleta bicicleta = Bicicleta.builder()
                .id(bicicletaId)
                .clienteId(clienteId)
                .build();
        given(bicicletaRepository.findByIdAndClienteTallerId(bicicletaId, tallerId))
                .willReturn(Optional.of(bicicleta));
        given(ordenRepository.existsByBicicletaId(bicicletaId)).willReturn(true);

        assertThatThrownBy(() -> bicicletaService.eliminar(bicicletaId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede eliminar una bicicleta con órdenes asociadas.");
    }
}
