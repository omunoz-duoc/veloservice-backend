package com.veloservice.clientes.application.usecase;

import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.application.dto.ClienteResult;
import com.veloservice.clientes.application.dto.MembresiaActualResult;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.domain.model.Membresia;
import com.veloservice.clientes.domain.model.SucursalCliente;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.MembresiaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.SucursalClienteRepository;
import com.veloservice.config.security.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public ClienteResult crear(ClienteCreateCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            throw new IllegalStateException("Operacion requiere contexto de sucursal");
        }

        Cliente cliente = clienteRepository.findByRut(command.getRut()).orElseGet(() -> Cliente.builder()
                .nombre(command.getNombre())
                .apellido(command.getApellido())
                .rut(command.getRut())
                .telefono(command.getTelefono())
                .email(command.getEmail())
                .direccion(command.getDireccion())
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
    public List<ClienteResult> listar() {
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
    public ClienteResult obtener(UUID id) {
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

    /**
     * Searches customers in the current tenant by name, email, phone, or RUT.
     *
     * @param texto search text
     * @return matching customers
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<ClienteResult> buscar(String texto) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null || !StringUtils.hasText(texto)) {
            return List.of();
        }
        return clienteRepository.buscarPorSucursal(sucursalId, texto.trim()).stream()
                .map(this::toBusquedaResult)
                .collect(Collectors.toList());
    }

    private ClienteResult toResponse(Cliente cliente, UUID sucursalId) {
        MembresiaActualResult membresiaActual = getMembresiaActual(cliente.getId(), sucursalId);
        return ClienteResult.builder()
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

    private ClienteResult toBusquedaResult(Cliente cliente) {
        return ClienteResult.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .rut(cliente.getRut())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .direccion(cliente.getDireccion())
                .build();
    }

    private MembresiaActualResult getMembresiaActual(UUID clienteId, UUID sucursalId) {
        return sucursalClienteRepository.findBySucursalIdAndClienteId(sucursalId, clienteId)
                .filter(vinculo -> vinculo.getMembresiaId() != null)
                .flatMap(vinculo -> membresiaRepository.findById(vinculo.getMembresiaId())
                        .map(this::toMembresiaActual))
                .orElse(null);
        }

    private MembresiaActualResult toMembresiaActual(Membresia membresia) {
        return MembresiaActualResult.builder()
                .nombre(membresia.getNombre())
                .descuento(membresia.getPorcentajeDescuento())
                .build();
    }
}