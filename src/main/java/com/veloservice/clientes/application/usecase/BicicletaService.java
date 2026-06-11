package com.veloservice.clientes.application.usecase;

import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.application.dto.BicicletaResult;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles bike operations with tenant isolation.
 */
@Service
@RequiredArgsConstructor
public class BicicletaService {

    private final BicicletaRepository bicicletaRepository;
    private final ClienteRepository clienteRepository;
    private final OrdenRepository ordenRepository;

    /**
     * Creates a bike for a customer in the current tenant.
     *
     * @param clienteId customer identifier
     * @param request bike request payload
     * @return created bike
     */
    @TenantOperation
    @Transactional
    public BicicletaResult crear(UUID clienteId, BicicletaCreateCommand command) {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new IllegalStateException("Operacion requiere contexto de taller");
        }

        Cliente cliente = clienteRepository.findByIdAndTallerId(clienteId, tallerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        OffsetDateTime now = OffsetDateTime.now();
        Bicicleta bicicleta = Bicicleta.builder()
                .clienteId(cliente.getId())
                .cliente(cliente)
                .marca(command.getMarca())
                .modelo(command.getModelo())
                .tipo(command.getTipo())
                .aro(command.getAro())
                .color(command.getColor())
                .numeroSerie(command.getNumeroSerie())
                .anio(command.getAnio())
                .notas(command.getNotas())
                .createdAt(now)
                .updatedAt(now)
                .build();

        bicicleta = bicicletaRepository.save(bicicleta);
        return toResponse(bicicleta);
    }

    /**
     * Lists bikes for a specific customer.
     *
     * @param clienteId customer identifier
     * @return customer bikes
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<BicicletaResult> listarPorCliente(UUID clienteId) {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new IllegalStateException("Operacion requiere contexto de taller");
        }
        clienteRepository.findByIdAndTallerId(clienteId, tallerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return bicicletaRepository.findByClienteId(clienteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lists all bikes for the current tenant.
     *
     * @return tenant bikes
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<BicicletaResult> listarTodas() {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            return List.of();
        }
        return bicicletaRepository.findAllByClienteTallerId(tallerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional
    public BicicletaResult actualizar(UUID id, BicicletaCreateCommand command) {
        Bicicleta bicicleta = findBicicletaDelTaller(id);
        bicicleta.setMarca(command.getMarca());
        bicicleta.setModelo(command.getModelo());
        bicicleta.setTipo(command.getTipo());
        bicicleta.setAro(command.getAro());
        bicicleta.setColor(command.getColor());
        bicicleta.setNumeroSerie(command.getNumeroSerie());
        bicicleta.setAnio(command.getAnio());
        bicicleta.setNotas(command.getNotas());
        bicicleta.setUpdatedAt(OffsetDateTime.now());
        return toResponse(bicicletaRepository.save(bicicleta));
    }

    @TenantOperation
    @Transactional
    public void eliminar(UUID id) {
        Bicicleta bicicleta = findBicicletaDelTaller(id);
        if (ordenRepository.existsByBicicletaId(id)) {
            throw new IllegalArgumentException("No se puede eliminar una bicicleta con órdenes asociadas.");
        }
        bicicletaRepository.delete(bicicleta);
    }

    private Bicicleta findBicicletaDelTaller(UUID id) {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new IllegalStateException("Operacion requiere contexto de taller");
        }
        return bicicletaRepository.findByIdAndClienteTallerId(id, tallerId)
                .orElseThrow(() -> new IllegalArgumentException("Bicicleta no encontrada"));
    }

    private BicicletaResult toResponse(Bicicleta bicicleta) {
        return BicicletaResult.builder()
                .id(bicicleta.getId())
                .clienteId(bicicleta.getClienteId())
                .marca(bicicleta.getMarca())
                .modelo(bicicleta.getModelo())
                .tipo(bicicleta.getTipo())
                .aro(bicicleta.getAro())
                .color(bicicleta.getColor())
                .numeroSerie(bicicleta.getNumeroSerie())
                .anio(bicicleta.getAnio())
                .notas(bicicleta.getNotas())
                .build();
    }
}
