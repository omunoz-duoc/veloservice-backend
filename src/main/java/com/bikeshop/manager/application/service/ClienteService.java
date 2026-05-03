package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.ClienteRequest;
import com.bikeshop.manager.application.dto.ClienteResponse;
import com.bikeshop.manager.application.dto.MembresiaActualResponse;
import com.bikeshop.manager.domain.tenant.Cliente;
import com.bikeshop.manager.domain.tenant.SucursalCliente;
import com.bikeshop.manager.domain.tenant.Membresia;
import com.bikeshop.manager.infrastructure.persistence.repository.ClienteRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.MembresiaRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.SucursalClienteRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.SucursalContext;
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
    private final SucursalClienteRepository sucursalClienteRepository;
    private final MembresiaRepository membresiaRepository;

    /**
     * Creates a customer in the current tenant.
     *
     * @param request customer request payload
     * @return created customer
     */
    @TenantOperation
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            throw new IllegalStateException("Operacion requiere contexto de sucursal");
        }

        Cliente cliente = clienteRepository.findByRut(request.getRut()).orElseGet(() -> Cliente.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .rut(request.getRut())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .direccion(request.getDireccion())
                .build());

        if (cliente.getId() == null) {
            cliente = clienteRepository.save(cliente);
        }

        if (!sucursalClienteRepository.existsBySucursalIdAndClienteId(sucursalId, cliente.getId())) {
            SucursalCliente vinculo = SucursalCliente.builder()
                    .sucursalId(sucursalId)
                    .clienteId(cliente.getId())
                    .build();
            sucursalClienteRepository.save(vinculo);
        }

        return toResponse(cliente, SucursalContext.getCurrentSucursal());
    }

    /**
     * Lists customers for the current tenant.
     *
     * @return tenant customers
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return sucursalClienteRepository.findAllBySucursalId(sucursalId).stream()
                .map(vinculo -> clienteRepository.findById(vinculo.getClienteId())
                        .orElseThrow(() -> new IllegalStateException("Cliente vinculado no encontrado")))
            .map(cliente -> toResponse(cliente, sucursalId))
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
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            throw new IllegalStateException("Operacion requiere contexto de sucursal");
        }
        sucursalClienteRepository.findBySucursalIdAndClienteId(sucursalId, id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return toResponse(cliente, sucursalId);
    }

        private ClienteResponse toResponse(Cliente cliente, UUID sucursalId) {
        MembresiaActualResponse membresiaActual = getMembresiaActual(cliente.getId(), sucursalId);
        return ClienteResponse.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .rut(cliente.getRut())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .direccion(cliente.getDireccion())
            .membresiaActual(membresiaActual)
                .build();
    }

            private MembresiaActualResponse getMembresiaActual(UUID clienteId, UUID sucursalId) {
            return sucursalClienteRepository.findBySucursalIdAndClienteId(sucursalId, clienteId)
                .filter(vinculo -> vinculo.getMembresiaId() != null)
                .flatMap(vinculo -> membresiaRepository.findById(vinculo.getMembresiaId())
                .map(this::toMembresiaActual))
            .orElse(null);
        }

        private MembresiaActualResponse toMembresiaActual(Membresia membresia) {
        return MembresiaActualResponse.builder()
            .nombre(membresia.getNombre())
            .descuento(membresia.getPorcentajeDescuento())
            .build();
        }
}
