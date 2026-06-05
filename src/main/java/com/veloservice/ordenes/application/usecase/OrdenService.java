package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
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
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.OrdenCatalogoResult;
import com.veloservice.ordenes.application.dto.OrdenCreateResult;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenDetalleBaseResult;
import com.veloservice.ordenes.application.dto.OrdenDetalleResult;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.OrdenProductoUpdateCommand;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.application.dto.OrdenUpdateCommand;
import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.ordenes.domain.PrioridadOrdenEnum;
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
import com.veloservice.servicios.domain.model.SucursalServicio;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.SucursalServicioRepository;
import com.veloservice.shared.application.exception.BadRequestException;
import com.veloservice.shared.application.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final SucursalServicioRepository sucursalServicioRepository;
    private final ProductoRepository productoRepository;
    private final OrdenServicioRepository ordenServicioRepository;
    private final OrdenProductoRepository ordenProductoRepository;
    private final SecuenciaService secuenciaService;
    private final ClienteService clienteService;
    private final BicicletaService bicicletaService;
    private final BicicletaRepository bicicletaRepository;
    private final ClienteRepository clienteRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;

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

    @Transactional(readOnly = true)
    public List<OrdenCatalogoResult> listarEstadosCatalogo() {
        return estadoOrdenRepository.findAllByOrderByOrdenAsc().stream()
                .map(estado -> new OrdenCatalogoResult(
                        estado.getCodigo(),
                        estado.getNombre(),
                        estado.getOrden(),
                        null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenCatalogoResult> listarTiposCatalogo() {
        return tipoOrdenRepository.findAllByOrderByCodigoAsc().stream()
                .map(tipo -> new OrdenCatalogoResult(
                        tipo.getCodigo(),
                        tipo.getNombre(),
                        null,
                        tipo.getActivo()
                ))
                .toList();
    }

    public List<OrdenCatalogoResult> listarPrioridadesCatalogo() {
        return List.of(PrioridadOrdenEnum.baja, PrioridadOrdenEnum.media, PrioridadOrdenEnum.alta).stream()
                .map(prioridad -> new OrdenCatalogoResult(prioridad.name(), nombrePrioridad(prioridad), null, true))
                .toList();
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
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        }

        if (tallerId != null) {
            return buscarEnTaller(id, tallerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        }

        throw new IllegalStateException("Contexto de taller o sucursal requerido");
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenDetalleResult obtenerDetalle(String id) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID tallerId = TallerContext.getCurrentTaller();

        OrdenDetalleBaseResult base;
        if (sucursalId != null) {
            base = buscarDetalleBaseEnSucursal(id, sucursalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        } else if (tallerId != null) {
            base = buscarDetalleBaseEnTaller(id, tallerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        } else {
            throw new IllegalStateException("Contexto de taller o sucursal requerido");
        }

        List<ComentarioResult> comentarios = comentarioRepository.findResultByOrdenId(base.id());
        List<MultimediaResult> multimedia = multimediaRepository.findResultByOrdenId(base.id());
        List<OrdenProductoResult> productos = ordenProductoRepository.findResultByOrdenId(base.id());
        List<OrdenServicioResult> servicios = ordenServicioRepository.findResultByOrdenId(base.id());
        return new OrdenDetalleResult(
            base.id(),
            base.numeroOrden(),
            base.tallerId(),
            base.sucursalId(),
            base.estadoId(), base.estadoCodigo(), base.estadoNombre(),
            base.tipoId(), base.tipoCodigo(), base.tipoNombre(),
            base.fechaIngreso(),
            base.fechaPrometida(),
            base.fechaEntrega(),
            base.diagnosticoInicial(),
            base.diagnosticoFinal(),
            base.observacionesCliente(),
            base.bicicletaId(), base.bicicletaMarca(), base.bicicletaModelo(), base.bicicletaTipo(), base.bicicletaColor(), base.bicicletaNumeroSerie(), base.bicicletaAro(), base.bicicletaAnio(), base.bicicletaFotoUrl(), base.bicicletaNotas(),
            base.clienteId(), base.clienteNombre(), base.clienteApellido(),
            base.clienteTelefono(), base.clienteEmail(), base.clienteRut(),
            base.mecanicoId(), base.mecanicoNombre(), base.mecanicoApellido(),
            base.prioridad(),
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
        UUID usuarioId = requerirUsuarioContext();
        Orden orden = buscarOrdenParaMutacion(id);
        OffsetDateTime now = OffsetDateTime.now();
        aplicarCambioEstado(orden, command.getCodigo(), command.getObservacion(), now, usuarioId);
        orden.setUpdatedAt(now);
        ordenRepository.save(orden);
    }

    @TenantOperation
    @Transactional
    public OrdenDetalleResult actualizar(String id, OrdenUpdateCommand command) {
        Orden orden = buscarOrdenParaMutacion(id);
        OffsetDateTime now = OffsetDateTime.now();
        boolean changed = false;

        if (hasText(command.getEstadoCodigo())) {
            aplicarCambioEstado(orden, command.getEstadoCodigo(), command.getEstadoObservacion(), now, requerirUsuarioContext());
            changed = true;
        } else if (command.getEstadoCodigo() != null) {
            throw new BadRequestException("estadoCodigo no puede estar vacio");
        }

        if (hasText(command.getTipoCodigo())) {
            TipoOrden tipo = tipoOrdenRepository.findByCodigo(command.getTipoCodigo())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de orden no encontrado: " + command.getTipoCodigo()));
            orden.setTipoId(tipo.getId());
            changed = true;
        } else if (command.getTipoCodigo() != null) {
            throw new BadRequestException("tipoCodigo no puede estar vacio");
        }

        if (hasText(command.getPrioridad())) {
            String prioridad = command.getPrioridad().trim().toLowerCase();
            try {
                PrioridadOrdenEnum.valueOf(prioridad);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Prioridad invalida: " + command.getPrioridad());
            }
            orden.setPrioridad(prioridad);
            changed = true;
        } else if (command.getPrioridad() != null) {
            throw new BadRequestException("prioridad no puede estar vacia");
        }

        if (command.getMecanicoId() != null) {
            if (!usuarioRepository.existsActiveMecanicoById(command.getMecanicoId())) {
                throw new ResourceNotFoundException("Mecanico no encontrado: " + command.getMecanicoId());
            }
            orden.setMecanicoId(command.getMecanicoId());
            changed = true;
        }

        if (hasItems(command.getProductosAgregar())) {
            validarOrdenPermiteModificarProductos(orden);
            agregarProductosAOrden(orden, command.getProductosAgregar(), now);
            changed = true;
        }

        if (hasItems(command.getProductosActualizar())) {
            validarOrdenPermiteModificarProductos(orden);
            actualizarProductosAOrden(orden, command.getProductosActualizar());
            changed = true;
        }

        if (hasItems(command.getProductosEliminar())) {
            validarOrdenPermiteModificarProductos(orden);
            eliminarProductosAOrden(orden, command.getProductosEliminar());
            changed = true;
        }

        if (changed) {
            orden.setUpdatedAt(now);
            ordenRepository.save(orden);
        }

        return obtenerDetalle(id);
    }

    @TenantOperation
    @Transactional
    public List<OrdenProductoResult> agregarProductos(UUID ordenId, List<OrdenProductoAddCommand> items) {
        Orden orden = buscarOrdenPorIdParaMutacion(ordenId);
        validarOrdenPermiteModificarProductos(orden);
        OffsetDateTime now = OffsetDateTime.now();
        return agregarProductosAOrden(orden, items, now);
    }

    private List<OrdenProductoResult> agregarProductosAOrden(Orden orden, List<OrdenProductoAddCommand> items, OffsetDateTime now) {
        Map<UUID, OrdenProducto> lineItemsByProducto = new LinkedHashMap<>();

        for (OrdenProductoAddCommand item : items) {
            if (item.getProductoId() == null) {
                throw new BadRequestException("productoId es requerido");
            }
            validarCantidad(item.getCantidad());
            Producto producto = buscarProductoDisponible(item.getProductoId(), orden.getSucursalId());
            boolean proporcionadoPorCliente = Boolean.TRUE.equals(item.getProporcionadoPorCliente());
            validarStockSuficiente(producto, item.getCantidad(), proporcionadoPorCliente);

            OrdenProducto lineItem = lineItemsByProducto.computeIfAbsent(item.getProductoId(), productoId ->
                    ordenProductoRepository.findByOrdenIdAndProductoId(orden.getId(), productoId)
                            .orElseGet(() -> OrdenProducto.builder()
                                    .ordenId(orden.getId())
                                    .productoId(productoId)
                                    .cantidad(0)
                                    .precioCostoSnapshot(producto.getPrecioCosto())
                                    .precioVentaSnapshot(producto.getPrecioVenta())
                                    .precioAplicado(producto.getPrecioVenta())
                                    .proporcionadoPorCliente(false)
                                    .createdAt(now)
                                    .build())
            );

            int nuevaCantidad = lineItem.getCantidad() + item.getCantidad();
            validarStockSuficiente(producto, nuevaCantidad, proporcionadoPorCliente);
            lineItem.setCantidad(nuevaCantidad);
            lineItem.setProporcionadoPorCliente(proporcionadoPorCliente);
            if (item.getNotas() != null) {
                lineItem.setNotas(item.getNotas());
            }
        }

        List<OrdenProducto> lineItems = new ArrayList<>(lineItemsByProducto.values());

        List<OrdenProducto> saved = ordenProductoRepository.saveAll(lineItems);
        List<UUID> ids = saved.stream().map(OrdenProducto::getId).toList();
        Map<UUID, OrdenProductoResult> resultsById = ordenProductoRepository.findResultByIdIn(ids).stream()
                .collect(Collectors.toMap(OrdenProductoResult::id, Function.identity()));
        return ids.stream().map(resultsById::get).toList();
    }

    private void actualizarProductosAOrden(Orden orden, List<OrdenProductoUpdateCommand> items) {
        List<OrdenProducto> lineItems = items.stream()
                .map(item -> {
                    if (item.getId() == null) {
                        throw new BadRequestException("id de producto asociado es requerido");
                    }
                    OrdenProducto lineItem = ordenProductoRepository.findByIdAndOrdenId(item.getId(), orden.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Producto asociado no encontrado: " + item.getId()
                            ));
                    Producto producto = buscarProductoDisponible(lineItem.getProductoId(), orden.getSucursalId());
                    Integer cantidad = item.getCantidad() != null ? item.getCantidad() : lineItem.getCantidad();
                    validarCantidad(cantidad);
                    boolean proporcionadoPorCliente = item.getProporcionadoPorCliente() != null
                            ? item.getProporcionadoPorCliente()
                            : Boolean.TRUE.equals(lineItem.getProporcionadoPorCliente());
                    validarStockSuficiente(producto, cantidad, proporcionadoPorCliente);

                    lineItem.setCantidad(cantidad);
                    lineItem.setProporcionadoPorCliente(proporcionadoPorCliente);
                    if (item.getNotas() != null) {
                        lineItem.setNotas(item.getNotas());
                    }
                    return lineItem;
                })
                .toList();

        ordenProductoRepository.saveAll(lineItems);
    }

    private void eliminarProductosAOrden(Orden orden, List<UUID> lineItemIds) {
        for (UUID lineItemId : lineItemIds) {
            if (lineItemId == null) {
                throw new BadRequestException("id de producto asociado es requerido");
            }
            int deleted = ordenProductoRepository.deleteByIdAndOrdenId(lineItemId, orden.getId());
            if (deleted == 0) {
                throw new ResourceNotFoundException("Producto asociado no encontrado: " + lineItemId);
            }
        }
    }

    @TenantOperation
    @Transactional
    public List<OrdenServicioResult> agregarServicios(UUID ordenId, List<OrdenServicioAddCommand> items) {
        Orden orden = buscarOrdenPorIdParaMutacion(ordenId);
        OffsetDateTime now = OffsetDateTime.now();

        List<OrdenServicio> lineItems = items.stream()
                .map(item -> {
                    Servicio servicio = servicioRepository.findById(item.getServicioId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Servicio no encontrado: " + item.getServicioId()
                            ));
                    if (!servicio.getTallerId().equals(orden.getTallerId())) {
                        throw new ResourceNotFoundException(
                                "Servicio " + item.getServicioId() + " no pertenece al taller de esta orden"
                        );
                    }

                    BigDecimal precioBase = sucursalServicioRepository
                            .findBySucursalIdAndServicioId(orden.getSucursalId(), item.getServicioId())
                            .map(SucursalServicio::getPrecioPersonalizado)
                            .filter(precio -> precio != null)
                            .orElse(servicio.getPrecioBase());

                    return OrdenServicio.builder()
                            .ordenId(ordenId)
                            .servicioId(item.getServicioId())
                            .precioBaseSnapshot(precioBase)
                            .precioAplicado(precioBase)
                            .descuentoAplicado(BigDecimal.ZERO)
                            .notas(item.getNotas())
                            .createdAt(now)
                            .build();
                })
                .toList();

        List<OrdenServicio> saved = ordenServicioRepository.saveAll(lineItems);
        List<UUID> ids = saved.stream().map(OrdenServicio::getId).toList();
        Map<UUID, OrdenServicioResult> resultsById = ordenServicioRepository.findResultByIdIn(ids).stream()
                .collect(Collectors.toMap(OrdenServicioResult::id, Function.identity()));
        return ids.stream().map(resultsById::get).toList();
    }

    private OrdenCreateResult crearEnSucursal(OrdenCreateCommand command, UUID tallerId, UUID sucursalId) {
        UUID clienteId = resolverCliente(command, tallerId);
        UUID bicicletaId = resolverBicicleta(command, clienteId, tallerId);

        EstadoOrden estado = estadoOrdenRepository.findByCodigo("recibida")
                .orElseThrow(() -> new IllegalStateException("Estado 'recibida' no configurado"));
        TipoOrden tipo = tipoOrdenRepository.findById(command.getTipoTrabajo())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de orden no encontrado: " + command.getTipoTrabajo()));

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
                        .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + servicioId));
                if (!servicio.getTallerId().equals(tallerId)) {
                    throw new ResourceNotFoundException("Servicio no encontrado: " + servicioId);
                }
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
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));
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
                throw new BadRequestException("La sucursal solicitada no coincide con el contexto actual");
            }
            validarSucursalDelTaller(contextSucursalId, tallerId);
            return contextSucursalId;
        }

        if (requestedSucursalId == null) {
            throw new BadRequestException("Se requiere sucursalId para crear una orden desde contexto de taller");
        }

        validarSucursalDelTaller(requestedSucursalId, tallerId);
        return requestedSucursalId;
    }

    private void validarSucursalDelTaller(UUID sucursalId, UUID tallerId) {
        if (!sucursalRepository.existsByIdAndTallerId(sucursalId, tallerId)) {
            throw new ResourceNotFoundException("Sucursal no pertenece al taller actual");
        }
    }

    private UUID resolverCliente(OrdenCreateCommand command, UUID tallerId) {
        if (command.getClienteId() != null) {
            if (!clienteRepository.existsByIdAndTallerId(command.getClienteId(), tallerId)) {
                throw new ResourceNotFoundException("Cliente no encontrado");
            }
            return command.getClienteId();
        }
        if (command.getClienteNuevo() != null) {
            ClienteCreateCommand cmd = command.getClienteNuevo();
            return clienteService.crear(cmd).getId();
        }
        throw new BadRequestException("Se requiere clienteId o clienteNuevo");
    }

    private UUID resolverBicicleta(OrdenCreateCommand command, UUID clienteId, UUID tallerId) {
        if (command.getBicicletaId() != null) {
            Bicicleta bicicleta = bicicletaRepository.findById(command.getBicicletaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bicicleta no encontrada"));
            if (!clienteRepository.existsByIdAndTallerId(bicicleta.getClienteId(), tallerId)) {
                throw new ResourceNotFoundException("Bicicleta no encontrada");
            }
            if (!bicicleta.getClienteId().equals(clienteId)) {
                throw new BadRequestException("La bicicleta no pertenece al cliente");
            }
            return bicicleta.getId();
        }
        if (command.getBicicletaNueva() != null) {
            BicicletaCreateCommand cmd = command.getBicicletaNueva();
            return bicicletaService.crear(clienteId, cmd).getId();
        }
        throw new BadRequestException("Se requiere bicicletaId o bicicletaNueva");
    }

    private Optional<OrdenDetalleBaseResult> buscarDetalleBaseEnSucursal(String id, UUID sucursalId) {
        return parseUuid(id)
                .flatMap(uuid -> ordenRepository.findDetalleBaseByIdAndSucursalId(uuid, sucursalId))
                .or(() -> ordenRepository.findDetalleBaseByNumeroOrdenAndSucursalId(id, sucursalId));
    }

    private Optional<OrdenDetalleBaseResult> buscarDetalleBaseEnTaller(String id, UUID tallerId) {
        return parseUuid(id)
                .flatMap(uuid -> ordenRepository.findDetalleBaseByIdAndTallerId(uuid, tallerId))
                .or(() -> ordenRepository.findDetalleBaseByNumeroOrdenAndTallerId(id, tallerId));
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
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        }

        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId != null) {
            return buscarOrdenEntityEnTaller(id, tallerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
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

    private Orden buscarOrdenPorIdParaMutacion(UUID ordenId) {
        Orden orden = ordenRepository.findById(ordenId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        UUID tallerId = TallerContext.getCurrentTaller();
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (tallerId == null && sucursalId == null) {
            throw new IllegalStateException("Contexto de taller o sucursal requerido");
        }
        if (tallerId != null && !orden.getTallerId().equals(tallerId)) {
            throw new AccessDeniedException("Acceso denegado");
        }
        if (sucursalId != null && !orden.getSucursalId().equals(sucursalId)) {
            throw new AccessDeniedException("Acceso denegado");
        }

        return orden;
    }

    /**
     * Intenta convertir el ID proporcionado a un UUID. Si la conversión falla, devuelve un Optional vacío.
     * @param id
     * @return
     */
    private void aplicarCambioEstado(Orden orden, String estadoCodigo, String observacion, OffsetDateTime now, UUID usuarioId) {
        UUID estadoAnteriorId = orden.getEstadoId();
        EstadoOrden nuevoEstado = estadoOrdenRepository.findByCodigo(estadoCodigo)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de orden no encontrado: " + estadoCodigo));

        orden.setEstadoId(nuevoEstado.getId());
        ordenEstadoRepository.save(OrdenEstado.builder()
                .ordenId(orden.getId())
                .usuarioId(usuarioId)
                .estadoAnteriorId(estadoAnteriorId)
                .estadoNuevoId(nuevoEstado.getId())
                .observacion(observacion)
                .createdAt(now)
                .build());
    }

    private UUID requerirUsuarioContext() {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) {
            throw new BadRequestException("Contexto de usuario requerido");
        }
        return usuarioId;
    }

    private Producto buscarProductoDisponible(UUID productoId, UUID sucursalId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productoId));
        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new ResourceNotFoundException("Producto no encontrado: " + productoId);
        }
        if (!producto.getSucursalId().equals(sucursalId)) {
            throw new ResourceNotFoundException(
                    "Producto " + productoId + " no pertenece a la sucursal de esta orden"
            );
        }
        return producto;
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new BadRequestException("Cantidad de producto debe ser mayor o igual a 1");
        }
    }

    private void validarStockSuficiente(Producto producto, Integer cantidad, boolean proporcionadoPorCliente) {
        if (proporcionadoPorCliente || producto.getStock() == null) {
            return;
        }
        if (producto.getStock() < cantidad) {
            throw new BadRequestException("Stock insuficiente para producto: " + producto.getId());
        }
    }

    private void validarOrdenPermiteModificarProductos(Orden orden) {
        if (orden.getEstadoId() == null) {
            return;
        }
        estadoOrdenRepository.findById(orden.getEstadoId())
                .map(EstadoOrden::getCodigo)
                .filter(codigo -> EstadoOrdenEnum.entregada.name().equals(codigo)
                        || EstadoOrdenEnum.cancelada.name().equals(codigo))
                .ifPresent(codigo -> {
                    throw new BadRequestException("No se pueden modificar productos de una orden " + codigo);
                });
    }

    public List<TipoOrden> listarTipos() {
        return tipoOrdenRepository.findAll();
    }

    private Optional<UUID> parseUuid(String id) {
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasItems(List<?> items) {
        return items != null && !items.isEmpty();
    }

    private String nombrePrioridad(PrioridadOrdenEnum prioridad) {
        String value = prioridad.name();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}
