package com.veloservice.clientes.application.usecase;

import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.application.dto.ClienteResult;
import com.veloservice.clientes.application.dto.ClienteResumenResult;
import com.veloservice.clientes.application.dto.MembresiaActualResult;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.domain.model.Membresia;
import com.veloservice.clientes.domain.model.SucursalCliente;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.MembresiaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.SucursalClienteRepository;
import com.veloservice.clientes.interfaces.mapper.ClienteMapper;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.finanzas.infraestructure.persistence.repository.CobroRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
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
    private final OrdenRepository ordenRepository;
    private final CobroRepository cobroRepository;

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

        return toResult(cliente, sucursalId);
    }

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
                .map(cliente -> toResult(cliente, sucursalId))
                .collect(Collectors.toList());
    }

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
        return toResult(cliente, sucursalId);
    }

    private ClienteResult toResult(Cliente cliente, UUID sucursalId) {
        MembresiaActualResult membresiaActual = getMembresiaActual(cliente.getId(), sucursalId);

        int bicicletasCount = cliente.getBicicletas() != null ? cliente.getBicicletas().size() : 0;

        List<UUID> bicicletaIds = cliente.getBicicletas() != null
                ? cliente.getBicicletas().stream()
                        .map(b -> b.getId())
                        .collect(Collectors.toList())
                : List.of();

        long ordenesCount = bicicletaIds.isEmpty() ? 0
                : ordenRepository.countByBicicletaIdIn(bicicletaIds);

        BigDecimal totalGastado = bicicletaIds.isEmpty() ? BigDecimal.ZERO
                : ordenRepository.findAllByBicicletaIdIn(bicicletaIds).stream()
                        .flatMap(o -> cobroRepository.findByOrdenId(o.getId()).stream())
                        .filter(c -> c.getEstado().name().equals("pagado"))
                        .map(c -> c.getTotal())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        String tipo;
        if (ordenesCount == 0) tipo = "nuevo";
        else if (ordenesCount <= 2) tipo = "regular";
        else if (ordenesCount <= 5) tipo = "frecuente";
        else tipo = "VIP";

        return ClienteResult.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .rut(cliente.getRut())
                .telefono(cliente.getTelefono())
                .email(cliente.getEmail())
                .direccion(cliente.getDireccion())
                .membresiaActual(membresiaActual)
                .tipo(tipo)
                .bicicletasCount(bicicletasCount)
                .ordenesCount((int) ordenesCount)
                .totalGastado(totalGastado)
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