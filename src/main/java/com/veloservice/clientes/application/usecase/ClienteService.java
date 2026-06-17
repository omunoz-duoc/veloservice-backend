package com.veloservice.clientes.application.usecase;

import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.clientes.application.dto.BicicletaDetalleResult;
import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.application.dto.ClienteDetalleResult;
import com.veloservice.clientes.application.dto.ClienteResult;
import com.veloservice.clientes.application.dto.MembresiaActualResult;
import com.veloservice.clientes.application.dto.OrdenResumenResult;
import com.veloservice.clientes.domain.model.Cliente;
import com.veloservice.clientes.domain.model.Membresia;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.MembresiaRepository;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.shared.application.exception.ConflictException;
import com.veloservice.shared.application.util.RutUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    private final MembresiaRepository membresiaRepository;
    private final OrdenRepository ordenRepository;

    @TenantOperation
    @Transactional
    public ClienteResult crear(ClienteCreateCommand command) {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new IllegalStateException("Operacion requiere contexto de taller");
        }

        String rut = RutUtils.normalize(command.getRut());
        if (StringUtils.hasText(rut)
                && clienteRepository.findByTallerIdAndRut(tallerId, rut).isPresent()) {
            throw new ConflictException("Ya existe un cliente con el RUT " + rut);
        }

        Cliente cliente = clienteRepository.save(nuevoCliente(command, tallerId));
        return toResult(cliente);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ClienteResult> listar() {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            return List.of();
        }
        return clienteRepository.findAllByTallerIdOrderByCreatedAtDesc(tallerId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public ClienteResult obtener(UUID id) {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new IllegalStateException("Operacion requiere contexto de taller");
        }
        Cliente cliente = clienteRepository.findByIdAndTallerId(id, tallerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        return toResult(cliente);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public ClienteDetalleResult obtenerDetalle(UUID id) {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new IllegalStateException("Operacion requiere contexto de taller");
        }
        Cliente cliente = clienteRepository.findByIdAndTallerId(id, tallerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        int bicicletasCount = cliente.getBicicletas() != null ? cliente.getBicicletas().size() : 0;
        List<BicicletaDetalleResult> bicicletas = cliente.getBicicletas() != null
                ? cliente.getBicicletas().stream()
                        .map(b -> new BicicletaDetalleResult(
                                b.getId(), b.getMarca(), b.getModelo(), b.getTipo(), b.getAro(),
                                b.getColor(), b.getNumeroSerie(), b.getAnio(), b.getNotas()))
                        .toList()
                : List.of();

        long otsCount = ordenRepository.countByClienteIdAndTallerId(id, tallerId);
        List<OrdenResumenResult> lastOts = ordenRepository.findResumenByClienteIdAndTallerId(
                        id, tallerId, Pageable.ofSize(5)).stream()
                .map(o -> new OrdenResumenResult(o.numeroOrden(), o.tipoOrden(), o.estadoOrden(), o.fechaIngreso()))
                .toList();

        return ClienteDetalleResult.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .apellido(cliente.getApellido())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .rut(cliente.getRut())
                .clienteDesde(cliente.getCreatedAt())
                .bicicletasCount(bicicletasCount)
                .bicicletas(bicicletas)
                .otsCount(otsCount)
                .lastOts(lastOts)
                .build();
    }

    @TenantOperation
    @Transactional
    public ClienteResult actualizar(UUID id, ClienteCreateCommand command) {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new IllegalStateException("Operacion requiere contexto de taller");
        }
        Cliente cliente = clienteRepository.findByIdAndTallerId(id, tallerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        cliente.setNombre(command.getNombre());
        cliente.setApellido(command.getApellido());
        cliente.setRut(RutUtils.normalize(command.getRut()));
        cliente.setTelefono(command.getTelefono());
        cliente.setEmail(command.getEmail());
        cliente.setDireccion(command.getDireccion());
        cliente.setUpdatedAt(OffsetDateTime.now());
        return toResult(clienteRepository.save(cliente));
    }

    private Cliente nuevoCliente(ClienteCreateCommand command, UUID tallerId) {
        OffsetDateTime now = OffsetDateTime.now();
        return Cliente.builder()
                .tallerId(tallerId)
                .nombre(command.getNombre())
                .apellido(command.getApellido())
                .rut(RutUtils.normalize(command.getRut()))
                .telefono(command.getTelefono())
                .email(command.getEmail())
                .direccion(command.getDireccion())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private ClienteResult toResult(Cliente cliente) {
        MembresiaActualResult membresiaActual = getMembresiaActual(cliente);

        int bicicletasCount = cliente.getBicicletas() != null ? cliente.getBicicletas().size() : 0;

        List<UUID> bicicletaIds = cliente.getBicicletas() != null
                ? cliente.getBicicletas().stream()
                        .map(b -> b.getId())
                        .collect(Collectors.toList())
                : List.of();

        long ordenesCount = 0L; // TODO: implementar countByBicicletaIdIn en OrdenRepository

        BigDecimal totalGastado = BigDecimal.ZERO; // TODO: implementar findAllByBicicletaIdIn en OrdenRepository

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

    private MembresiaActualResult getMembresiaActual(Cliente cliente) {
        if (cliente.getMembresiaId() == null) {
            return null;
        }
        return membresiaRepository.findById(cliente.getMembresiaId())
                .map(this::toMembresiaActual)
                .orElse(null);
    }

    private MembresiaActualResult toMembresiaActual(Membresia membresia) {
        return MembresiaActualResult.builder()
                .nombre(membresia.getNombre())
                .descuento(membresia.getPorcentajeDescuento())
                .build();
    }
}
