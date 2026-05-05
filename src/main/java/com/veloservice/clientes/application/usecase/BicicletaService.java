package com.veloservice.clientes.application.usecase;

import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.application.dto.BicicletaResult;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.SucursalClienteRepository;
import com.veloservice.config.security.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SucursalClienteRepository sucursalClienteRepository;

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
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            throw new IllegalStateException("Operacion requiere contexto de sucursal");
        }

        sucursalClienteRepository.findBySucursalIdAndClienteId(sucursalId, clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no pertenece a la sucursal actual"));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        Bicicleta bicicleta = Bicicleta.builder()
                .cliente(cliente)
            .marca(command.getMarca())
            .modelo(command.getModelo())
            .tipo(command.getTipo())
            .aro(command.getAro())
            .color(command.getColor())
            .numeroSerie(command.getNumeroSerie())
            .anio(command.getAnio())
            .notas(command.getNotas())
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
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        sucursalClienteRepository.findBySucursalIdAndClienteId(sucursalId, clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no pertenece a la sucursal actual"));
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
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return sucursalClienteRepository.findAllBySucursalId(sucursalId).stream()
                .flatMap(vinculo -> bicicletaRepository.findByClienteId(vinculo.getClienteId()).stream())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private BicicletaResult toResponse(Bicicleta bicicleta) {
        return BicicletaResult.builder()
                .id(bicicleta.getId())
                .clienteId(bicicleta.getCliente().getId())
                .marca(bicicleta.getMarca())
                .modelo(bicicleta.getModelo())
                .tipo(bicicleta.getTipo())
                .aro(bicicleta.getAro())
                .color(bicicleta.getColor())
                .numeroSerie(bicicleta.getNumeroSerie())
                .anio(bicicleta.getAnio())
                .build();
    }
}