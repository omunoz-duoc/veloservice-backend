package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.BicicletaRequest;
import com.bikeshop.manager.application.dto.BicicletaResponse;
import com.bikeshop.manager.domain.tenant.Bicicleta;
import com.bikeshop.manager.domain.tenant.Cliente;
import com.bikeshop.manager.infrastructure.persistence.repository.BicicletaRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.ClienteRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.SucursalClienteRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
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
    public BicicletaResponse crear(UUID clienteId, BicicletaRequest request) {
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
                .marca(request.getMarca())
                .modelo(request.getModelo())
                .tipo(request.getTipo())
                .aro(request.getAro())
                .color(request.getColor())
                .numeroSerie(request.getNumeroSerie())
                .anio(request.getAnio())
                .notas(request.getNotas())
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
    public List<BicicletaResponse> listarPorCliente(UUID clienteId) {
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
    public List<BicicletaResponse> listarTodas() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return sucursalClienteRepository.findAllBySucursalId(sucursalId).stream()
                .flatMap(vinculo -> bicicletaRepository.findByClienteId(vinculo.getClienteId()).stream())
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private BicicletaResponse toResponse(Bicicleta bicicleta) {
        return BicicletaResponse.builder()
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
