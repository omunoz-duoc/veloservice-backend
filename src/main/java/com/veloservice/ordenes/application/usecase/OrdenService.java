package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.application.usecase.BicicletaService;
import com.veloservice.clientes.application.usecase.ClienteService;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.OrdenCreateResult;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenDetalleResult;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.domain.model.EstadoOrden;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenEstado;
import com.veloservice.ordenes.domain.model.OrdenProducto;
import com.veloservice.ordenes.domain.model.OrdenServicio;
import com.veloservice.ordenes.domain.model.TipoOrden;
import com.veloservice.ordenes.infraestructure.persistence.repository.ComentarioRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.EstadoOrdenCatalogRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenEstadoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.TipoOrdenRepository;
import com.veloservice.servicios.domain.model.Servicio;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ComentarioRepository comentarioRepository;
    private final MultimediaRepository multimediaRepository;
    private final EstadoOrdenCatalogRepository estadoOrdenRepository;
    private final OrdenEstadoRepository ordenEstadoRepository;
    private final TipoOrdenRepository tipoOrdenRepository;
    private final ServicioRepository servicioRepository;
    private final ProductoRepository productoRepository;
    private final OrdenServicioRepository ordenServicioRepository;
    private final OrdenProductoRepository ordenProductoRepository;
    private final SecuenciaService secuenciaService;
    private final ClienteService clienteService;
    private final BicicletaService bicicletaService;
    private final BicicletaRepository bicicletaRepository;
    private final ClienteRepository clienteRepository;
    private final SucursalRepository sucursalRepository;

    /**
     * Lista todas las órdenes asociadas al taller o sucursal actual. Si ambos contextos están presentes, se prioriza el contexto de 
     * sucursal.
     * @return
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenReadResult> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            return ordenRepository.findReadBySucursalId(sucursalId);
        }

        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId != null) {
            return ordenRepository.findReadByTallerId(tallerId);
        }

        throw new IllegalStateException("Contexto de taller o sucursal requerido");
    }

    /**
     * Obtiene una orden por su ID o número de orden, buscando primero en el contexto de sucursal (si está presente) y luego en el 
     * contexto de taller.
     * @param id
     * @return
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenReadResult obtener(String id) {
        UUID tallerId = TallerContext.getCurrentTaller();
        UUID sucursalId = SucursalContext.getCurrentSucursal();

        if (sucursalId != null) {
            return buscarEnSucursal(id, sucursalId)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        }

        if (tallerId != null) {
            return buscarEnTaller(id, tallerId)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        }

        throw new IllegalStateException("Contexto de taller o sucursal requerido");
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenDetalleResult obtenerDetalle(String id) {
        OrdenReadResult orden = obtener(id);
        List<ComentarioResult> comentarios = comentarioRepository.findResultByOrdenId(orden.id());
        List<MultimediaResult> multimedia = multimediaRepository.findResultByOrdenId(orden.id());
        List<OrdenProductoResult> productos = ordenProductoRepository.findResultByOrdenId(orden.id());
        List<OrdenServicioResult> servicios = ordenServicioRepository.findResultByOrdenId(orden.id());
        return new OrdenDetalleResult(
            orden.id(),
            orden.numeroOrden(),
            orden.tallerId(),
            orden.sucursalId(),
            orden.estadoId(), orden.estadoCodigo(), orden.estadoNombre(),
            orden.tipoId(), orden.tipoCodigo(), orden.tipoNombre(),
            orden.fechaIngreso(),
            orden.fechaPrometida(),
            orden.fechaEntrega(),
            orden.diagnosticoInicial(),
            orden.diagnosticoFinal(),
            orden.observacionesCliente(),
            orden.bicicletaId(), orden.bicicletaMarca(), orden.bicicletaModelo(),
            orden.bicicletaTipo(), orden.bicicletaColor(), orden.bicicletaNumeroSerie(),
            orden.clienteId(), orden.clienteNombre(), orden.clienteApellido(),
            orden.clienteTelefono(), orden.clienteEmail(), orden.clienteRut(),
            orden.mecanicoId(), orden.mecanicoNombre(), orden.mecanicoApellido(),
            orden.prioridad(),
            comentarios,
            multimedia,
            productos,
            servicios
        );
    }

    @TenantOperation
    @Transactional
    public OrdenCreateResult crear(OrdenCreateCommand command) {
        UUID tallerId = TallerContext.getCurrentTaller();
        UUID contextSucursalId = SucursalContext.getCurrentSucursal();

        if (tallerId == null) {
            throw new IllegalStateException("Se requiere contexto de taller para crear una orden");
        }

        UUID sucursalId = resolverSucursal(tallerId, contextSucursalId, command.getSucursalId());
        boolean sucursalTemporal = contextSucursalId == null;
        if (sucursalTemporal) {
            SucursalContext.setCurrentSucursal(sucursalId);
        }

        try {
            return crearEnSucursal(command, tallerId, sucursalId);
        } finally {
            if (sucursalTemporal) {
                SucursalContext.clear();
            }
        }
    }

    @TenantOperation
    @Transactional
    public void cambiarEstado(String id, OrdenEstadoChangeCommand command) {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) {
            throw new IllegalStateException("Contexto de usuario requerido");
        }

        Orden orden = buscarOrdenParaMutacion(id);
        UUID estadoAnteriorId = orden.getEstadoId();
        EstadoOrden nuevoEstado = estadoOrdenRepository.findByCodigo(command.getCodigo())
                .orElseThrow(() -> new IllegalArgumentException("Estado de orden no encontrado: " + command.getCodigo()));
        OffsetDateTime now = OffsetDateTime.now();

        orden.setEstadoId(nuevoEstado.getId());
        orden.setUpdatedAt(now);
        ordenRepository.save(orden);

        ordenEstadoRepository.save(OrdenEstado.builder()
                .ordenId(orden.getId())
                .usuarioId(usuarioId)
                .estadoAnteriorId(estadoAnteriorId)
                .estadoNuevoId(nuevoEstado.getId())
                .observacion(command.getObservacion())
                .createdAt(now)
                .build());
    }

    private OrdenCreateResult crearEnSucursal(OrdenCreateCommand command, UUID tallerId, UUID sucursalId) {
        UUID clienteId = resolverCliente(command, tallerId);
        UUID bicicletaId = resolverBicicleta(command, clienteId, tallerId);

        EstadoOrden estado = estadoOrdenRepository.findByCodigo("recibida")
                .orElseThrow(() -> new IllegalStateException("Estado 'recibida' no configurado"));
        TipoOrden tipo = tipoOrdenRepository.findByCodigo(command.getTipoTrabajo())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de orden no encontrado: " + command.getTipoTrabajo()));

        String numeroOrden = secuenciaService.generarNumeroOrden(tallerId);

        OffsetDateTime now = OffsetDateTime.now();
        Orden orden = Orden.builder()
                .tallerId(tallerId)
                .sucursalId(sucursalId)
                .bicicletaId(bicicletaId)
                .mecanicoId(command.getMecanicoId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden(numeroOrden)
                .diagnosticoInicial(command.getDiagnosticoInicial())
                .observacionesCliente(command.getObservacionesCliente())
                .prioridad(command.getPrioridad() != null ? command.getPrioridad().name() : null)
                .fechaIngreso(now)
                .fechaPrometida(command.getFechaPrometida() != null
                        ? command.getFechaPrometida().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime()
                        : null)
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();
        orden = ordenRepository.save(orden);
        UUID ordenId = orden.getId();

        if (command.getServicios() != null) {
            for (UUID servicioId : command.getServicios()) {
                Servicio servicio = servicioRepository.findById(servicioId)
                        .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado: " + servicioId));
                ordenServicioRepository.save(OrdenServicio.builder()
                        .ordenId(ordenId)
                        .servicioId(servicioId)
                        .precioBaseSnapshot(servicio.getPrecioBase())
                        .precioAplicado(servicio.getPrecioBase())
                        .descuentoAplicado(BigDecimal.ZERO)
                        .createdAt(now)
                        .build());
            }
        }

        if (command.getProductos() != null) {
            for (OrdenCreateCommand.ProductoItem item : command.getProductos()) {
                Producto producto = productoRepository.findByIdAndSucursalId(item.getProductoId(), sucursalId)
                        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + item.getProductoId()));
                ordenProductoRepository.save(OrdenProducto.builder()
                        .ordenId(ordenId)
                        .productoId(item.getProductoId())
                        .cantidad(item.getCantidad())
                        .precioCostoSnapshot(producto.getPrecioCosto())
                        .precioVentaSnapshot(producto.getPrecioVenta())
                        .precioAplicado(producto.getPrecioVenta())
                        .proporcionadoPorCliente(false)
                        .createdAt(now)
                        .build());
            }
        }

        return new OrdenCreateResult(ordenId, numeroOrden);
    }

    private UUID resolverSucursal(UUID tallerId, UUID contextSucursalId, UUID requestedSucursalId) {
        if (contextSucursalId != null) {
            if (requestedSucursalId != null && !contextSucursalId.equals(requestedSucursalId)) {
                throw new IllegalArgumentException("La sucursal solicitada no coincide con el contexto actual");
            }
            validarSucursalDelTaller(contextSucursalId, tallerId);
            return contextSucursalId;
        }

        if (requestedSucursalId == null) {
            throw new IllegalArgumentException("Se requiere sucursalId para crear una orden desde contexto de taller");
        }

        validarSucursalDelTaller(requestedSucursalId, tallerId);
        return requestedSucursalId;
    }

    private void validarSucursalDelTaller(UUID sucursalId, UUID tallerId) {
        if (!sucursalRepository.existsByIdAndTallerId(sucursalId, tallerId)) {
            throw new IllegalArgumentException("Sucursal no pertenece al taller actual");
        }
    }

    private UUID resolverCliente(OrdenCreateCommand command, UUID tallerId) {
        if (command.getClienteId() != null) {
            if (!clienteRepository.existsByIdAndTallerId(command.getClienteId(), tallerId)) {
                throw new IllegalArgumentException("Cliente no encontrado");
            }
            return command.getClienteId();
        }
        if (command.getClienteNuevo() != null) {
            ClienteCreateCommand cmd = command.getClienteNuevo();
            return clienteService.crear(cmd).getId();
        }
        throw new IllegalArgumentException("Se requiere clienteId o clienteNuevo");
    }

    private UUID resolverBicicleta(OrdenCreateCommand command, UUID clienteId, UUID tallerId) {
        if (command.getBicicletaId() != null) {
            Bicicleta bicicleta = bicicletaRepository.findById(command.getBicicletaId())
                    .orElseThrow(() -> new IllegalArgumentException("Bicicleta no encontrada"));
            if (!clienteRepository.existsByIdAndTallerId(bicicleta.getClienteId(), tallerId)) {
                throw new IllegalArgumentException("Bicicleta no encontrada");
            }
            if (!bicicleta.getClienteId().equals(clienteId)) {
                throw new IllegalArgumentException("La bicicleta no pertenece al cliente");
            }
            return bicicleta.getId();
        }
        if (command.getBicicletaNueva() != null) {
            BicicletaCreateCommand cmd = command.getBicicletaNueva();
            return bicicletaService.crear(clienteId, cmd).getId();
        }
        throw new IllegalArgumentException("Se requiere bicicletaId o bicicletaNueva");
    }

    /**
     * Busca una orden por su ID o número de orden en el contexto de taller.
     * @param id
     * @param tallerId
     * @return
     */
    private Optional<OrdenReadResult> buscarEnTaller(String id, UUID tallerId) {
        Optional<OrdenReadResult> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findReadByIdAndTallerId(uuid, tallerId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findReadByNumeroOrdenAndTallerId(id, tallerId);
    }

    /**
     * Busca una orden por su ID o número de orden en el contexto de sucursal.
     * @param id
     * @param sucursalId
     * @return
     */
    private Optional<OrdenReadResult> buscarEnSucursal(String id, UUID sucursalId) {
        Optional<OrdenReadResult> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findReadByIdAndSucursalId(uuid, sucursalId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findReadByNumeroOrdenAndSucursalId(id, sucursalId);
    }

    private Orden buscarOrdenParaMutacion(String id) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            return buscarOrdenEntityEnSucursal(id, sucursalId)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        }

        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId != null) {
            return buscarOrdenEntityEnTaller(id, tallerId)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        }

        throw new IllegalStateException("Contexto de taller o sucursal requerido");
    }

    private Optional<Orden> buscarOrdenEntityEnTaller(String id, UUID tallerId) {
        Optional<Orden> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findByIdAndTallerId(uuid, tallerId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findByNumeroOrdenAndTallerId(id, tallerId);
    }

    private Optional<Orden> buscarOrdenEntityEnSucursal(String id, UUID sucursalId) {
        Optional<Orden> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findByIdAndSucursalId(uuid, sucursalId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findByNumeroOrdenAndSucursalId(id, sucursalId);
    }

    /**
     * Intenta convertir el ID proporcionado a un UUID. Si la conversión falla, devuelve un Optional vacío.
     * @param id
     * @return
     */
    private Optional<UUID> parseUuid(String id) {
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
