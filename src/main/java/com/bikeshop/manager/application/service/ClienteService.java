package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.ClienteRequest;
import com.bikeshop.manager.application.dto.ClienteResponse;
import com.bikeshop.manager.domain.tenant.Cliente;
import com.bikeshop.manager.domain.tenant.TallerCliente;
import com.bikeshop.manager.infrastructure.persistence.repository.ClienteRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.TallerClienteRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles customer operations with tenant isolation.
 */
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TallerClienteRepository tallerClienteRepository;

    /**
     * Creates a customer in the current tenant.
     *
     * @param request customer request payload
     * @return created customer
     */
    @TenantOperation
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        UUID tallerId = TenantContext.getCurrentTenant();
        if (tallerId == null) {
            throw new IllegalStateException("Operacion requiere contexto de taller");
        }

        if (clienteRepository.existsByTallerIdAndRut(tallerId, request.getRut())) {
            throw new IllegalArgumentException("Ya existe un cliente con RUT " + request.getRut() + " en este taller");
        }

        Cliente cliente = Cliente.builder()
                .tallerId(tallerId)
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .rut(request.getRut())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .direccion(request.getDireccion())
                .build();

        cliente = clienteRepository.save(cliente);

        TallerCliente vinculo = TallerCliente.builder()
                .tallerId(tallerId)
                .clienteId(cliente.getId())
                .build();
        tallerClienteRepository.save(vinculo);

        return toResponse(cliente);
    }

    /**
     * Lists customers for the current tenant.
     *
     * @return tenant customers
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        UUID tallerId = TenantContext.getCurrentTenant();
        if (tallerId == null) {
            return List.of();
        }
        return clienteRepository.findAllByTallerId(tallerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a customer by identifier.
     *
     * @param id customer identifier
     * @return customer response
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public ClienteResponse obtener(UUID id) {
        UUID tallerId = TenantContext.getCurrentTenant();
        Cliente cliente = clienteRepository.findByIdAndTallerId(id, tallerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return toResponse(cliente);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .rut(cliente.getRut())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .direccion(cliente.getDireccion())
                .build();
    }
}
