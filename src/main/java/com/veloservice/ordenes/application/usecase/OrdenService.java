package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.domain.model.Usuario;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.enums.TipoMovimientoEnum;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.ordenes.application.dto.NuevaOrdenCommand;
import com.veloservice.ordenes.application.dto.MultimediaCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenActividadRecienteResult;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenMetricasResult;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenResumenResult;
import com.veloservice.ordenes.application.dto.OrdenResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.OrdenUrgenteResult;
import com.veloservice.ordenes.domain.model.Multimedia;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenEstado;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenEstadoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;
import java.util.UUID;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles work order lifecycle operations.
 */
@Service
@RequiredArgsConstructor
public class OrdenService {

    private static final Map<EstadoOrdenEnum, Set<EstadoOrdenEnum>> TRANSICIONES_VALIDAS;

    static {
        Map<EstadoOrdenEnum, Set<EstadoOrdenEnum>> transiciones = new EnumMap<>(EstadoOrdenEnum.class);
        transiciones.put(EstadoOrdenEnum.recibida,
                EnumSet.of(EstadoOrdenEnum.en_diagnostico));
        transiciones.put(EstadoOrdenEnum.en_diagnostico,
                EnumSet.of(EstadoOrdenEnum.esperando_repuestos));
        transiciones.put(EstadoOrdenEnum.esperando_repuestos,
                EnumSet.of(EstadoOrdenEnum.en_reparacion));
        transiciones.put(EstadoOrdenEnum.en_reparacion,
                EnumSet.of(EstadoOrdenEnum.control_calidad));
        transiciones.put(EstadoOrdenEnum.control_calidad,
                EnumSet.of(EstadoOrdenEnum.lista_para_entrega));
        transiciones.put(EstadoOrdenEnum.lista_para_entrega,
                EnumSet.of(EstadoOrdenEnum.entregada));
        TRANSICIONES_VALIDAS = Map.copyOf(transiciones);
    }

    private final OrdenRepository ordenRepository;
    private final OrdenEstadoRepository ordenEstadoRepository;
    private final MultimediaRepository multimediaRepository;
    private final ServicioRepository servicioRepository;
    private final OrdenServicioRepository ordenServicioRepository;
    private final ProductoRepository productoRepository;
    private final OrdenProductoRepository ordenProductoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final SecuenciaService secuenciaService;
    private final BicicletaRepository bicicletaRepository;
    private final UsuarioRepository usuarioRepository;

    @TenantOperation
    @Transactional
    public OrdenResult crearNuevaOrden(NuevaOrdenCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de sucursal/usuario requerido");
        }
        // TODO: revisar DTO NuevaOrdenCommand - multimedia no existe

        String numeroOrden = secuenciaService.generarNumeroOrden(sucursalId);

        Orden orden = Orden.builder()
                .sucursalId(sucursalId)
                .bicicletaId(command.getBicicletaId())
                .mecanicoId(usuarioId)
                .mecanicoAsignadoId(command.getMecanicoAsignadoId())
                .numeroOrden(numeroOrden)
                .estado(EstadoOrdenEnum.recibida)
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(OffsetDateTime.now())
                .build();

        orden = ordenRepository.save(orden);

        registrarEstado(orden.getId(), sucursalId, usuarioId, null, EstadoOrdenEnum.recibida,
                "Creacion de orden de trabajo");

        return toResult(orden);
    }

    @TenantOperation
    @Transactional
    public OrdenResult cambiarEstado(UUID ordenId, OrdenEstadoChangeCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();

        Orden orden = ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        EstadoOrdenEnum estadoActual = orden.getEstado();
        EstadoOrdenEnum estadoNuevo = command.getNuevoEstado();

        Set<EstadoOrdenEnum> permitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoActual, Set.of());
        if (!permitidos.contains(estadoNuevo)) {
            throw new IllegalArgumentException("Transicion no permitida: " + estadoActual + "->" + estadoNuevo);
        }

        if (EstadoOrdenEnum.lista_para_entrega.equals(estadoNuevo)) {
            boolean tieneEvidenciaTecnica = multimediaRepository.existsByOrdenIdAndEtapa(
                    ordenId, EtapaMultimediaEnum.reparacion);
            if (!tieneEvidenciaTecnica) {
                throw new IllegalArgumentException(
                        "No se puede cambiar a listo sin evidencia tecnica final (RN02)");
            }
        }



        orden.setEstado(estadoNuevo);
        if (EstadoOrdenEnum.entregada.equals(estadoNuevo)) {
            orden.setFechaEntrega(OffsetDateTime.now());
        }
        orden = ordenRepository.save(orden);

        registrarEstado(ordenId, sucursalId, usuarioId, estadoActual, estadoNuevo, command.getObservacion());

        return toResult(orden);
    }

    @TenantOperation
    @Transactional
    public OrdenResult agregarServicio(UUID ordenId, OrdenServicioAddCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();

        Orden orden = ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        var servicio = servicioRepository.findById(command.getServicioId())
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        var ordenServicio = com.veloservice.ordenes.domain.model.OrdenServicio.builder()
                .ordenId(ordenId)
                .servicioId(servicio.getId())
                .precioBaseSnapshot(servicio.getPrecioBase())
                .precioAplicado(servicio.getPrecioBase())
                .descuentoAplicado(BigDecimal.ZERO)
                .notas(command.getNotas())
                .build();

        ordenServicioRepository.save(ordenServicio);
        return toResult(orden);
    }

    @TenantOperation
    @Transactional
    public OrdenResult agregarProducto(UUID ordenId, OrdenProductoAddCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();

        Orden orden = ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        var producto = productoRepository.findByIdAndSucursalId(command.getProductoId(), sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la sucursal"));

        if (!Boolean.TRUE.equals(command.getProporcionadoPorCliente())) {
            if (producto.getStock() < command.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto solicitado");
            }
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior - command.getCantidad());
            productoRepository.save(producto);

            var movimiento = com.veloservice.inventario.domain.model.MovimientoStock.builder()
                    .productoId(producto.getId())
                    .ordenId(ordenId)
                    .usuarioId(usuarioId)
                    .tipo(TipoMovimientoEnum.salida)
                    .cantidad(command.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockPosterior(producto.getStock())
                    .motivo("Consumo por orden de trabajo")
                    .build();
            movimientoStockRepository.save(movimiento);
        }

        var ordenProducto = com.veloservice.ordenes.domain.model.OrdenProducto.builder()
                .ordenId(ordenId)
                .productoId(producto.getId())
                .cantidad(command.getCantidad())
                .precioCostoSnapshot(producto.getPrecioCosto())
                .precioVentaSnapshot(producto.getPrecioVenta())
                .precioAplicado(producto.getPrecioVenta().multiply(new BigDecimal(command.getCantidad())))
                .proporcionadoPorCliente(Boolean.TRUE.equals(command.getProporcionadoPorCliente()))
                .notas(command.getNotas())
                .build();

        ordenProductoRepository.save(ordenProducto);
        return toResult(orden);
        }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenProductoResult> listarProductosPorOrden(UUID ordenId) {
        return ordenProductoRepository.findByOrdenId(ordenId).stream()
                .map(op -> {
                    var producto = productoRepository.findById(op.getProductoId()).orElse(null);
                    return OrdenProductoResult.builder()
                            .id(op.getId())
                            .productoId(op.getProductoId())
                            .nombre(producto != null ? producto.getNombre() : "")
                            .sku(producto != null ? producto.getSku() : "")
                            .cantidad(op.getCantidad())
                            .precioVenta(op.getPrecioVentaSnapshot())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional
    public void eliminarProducto(UUID ordenId, UUID productoId) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();

        ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        var ordenProducto = ordenProductoRepository.findByOrdenIdAndProductoId(ordenId, productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la orden"));

        if (!Boolean.TRUE.equals(ordenProducto.getProporcionadoPorCliente())) {
            var producto = productoRepository.findByIdAndSucursalId(productoId, sucursalId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la sucursal"));
            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior + ordenProducto.getCantidad());
            productoRepository.save(producto);

            var movimiento = com.veloservice.inventario.domain.model.MovimientoStock.builder()
                    .productoId(productoId)
                    .ordenId(ordenId)
                    .usuarioId(usuarioId)
                    .tipo(TipoMovimientoEnum.entrada)
                    .cantidad(ordenProducto.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockPosterior(producto.getStock())
                    .motivo("Devolución por eliminación de orden")
                    .build();
            movimientoStockRepository.save(movimiento);
        }

        ordenProductoRepository.delete(ordenProducto);
    }

    /**
     * Lists work orders for the current tenant.
     *
     * @return work orders
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenResult> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID mecanicoId = UsuarioContext.getCurrentUser();
        return ordenRepository.findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(sucursalId, mecanicoId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenResumenResult> listarResumen() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        return List.of(); // TODO: implementar findResumenBySucursalIdOrderByFechaIngresoDesc en repository
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenResult obtener(String id) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        Orden orden;
        
        // Try to parse as UUID first
        try {
            UUID uuidId = UUID.fromString(id);
            orden = ordenRepository.findByIdAndSucursalId(uuidId, sucursalId)
                    .orElse(null);
        } catch (IllegalArgumentException e) {
            // Not a valid UUID, treat as external_id
            orden = null;
        }
        
        // If not found by UUID, try by external_id
        if (orden == null) {
            orden = ordenRepository.findByExternalIdAndSucursalId(id, sucursalId)
                    .orElse(null);
        }
        
        if (orden == null) {
            throw new IllegalArgumentException("Orden no encontrada");
        }
        
        return toResult(orden);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenResult> listarUrgentes() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        return ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId).stream()
                .filter(o -> o.getFechaPrometida() != null
                        && o.getFechaPrometida().isBefore(OffsetDateTime.now())
                        && !o.getEstado().equals(EstadoOrdenEnum.entregada)
)
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenMetricasResult metricas() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        List<Orden> ordenes = ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId);

        long recibidas = ordenes.stream()
                .filter(o -> o.getEstado().equals(EstadoOrdenEnum.recibida)).count();
        long enProceso = ordenes.stream()
                .filter(o -> o.getEstado().equals(EstadoOrdenEnum.en_diagnostico)).count();
        long listas = ordenes.stream()
                .filter(o -> o.getEstado().equals(EstadoOrdenEnum.lista_para_entrega)).count();
        long entregadas = ordenes.stream()
                .filter(o -> o.getEstado().equals(EstadoOrdenEnum.entregada)).count();

        return new OrdenMetricasResult(recibidas, enProceso, listas, entregadas);
    }

    private void registrarEstado(UUID ordenId, UUID sucursalId, UUID usuarioId,
                                 EstadoOrdenEnum anterior, EstadoOrdenEnum nuevo, String observacion) {
        OrdenEstado auditoria = OrdenEstado.builder()
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .observacion(observacion)
                .build();
        ordenEstadoRepository.save(auditoria);
    }

    private UUID resolveCliente(NuevaOrdenCommand command, UUID sucursalId) {
        // TODO: reimplementar resolución de cliente (requiere ClienteRepository y SucursalClienteRepository)
        if (command.getClienteId() != null) {
            return command.getClienteId();
        }
        throw new UnsupportedOperationException("resolveCliente no implementado");
    }

    private UUID resolveBicicleta(NuevaOrdenCommand command, UUID clienteId, UUID sucursalId) {
        // TODO: reimplementar resolución de bicicleta (requiere ClienteRepository)
        if (command.getBicicletaId() != null) {
            Bicicleta bicicleta = bicicletaRepository.findById(command.getBicicletaId())
                    .orElseThrow(() -> new IllegalArgumentException("Bicicleta no encontrada"));
            return bicicleta.getId();
        }
        throw new UnsupportedOperationException("resolveBicicleta no implementado");
    }

    private UUID resolveMecanicoAsignado(UUID mecanicoAsignadoId, UUID sucursalId) {
        if (mecanicoAsignadoId == null) {
            return null;
        }
        boolean exists = usuarioRepository.existsByIdAndSucursalIdAndRolNombreAndActivoTrue(
                mecanicoAsignadoId, sucursalId, "MECANICO");
        if (!exists) {
            throw new IllegalArgumentException("Mecanico asignado no valido");
        }
        return mecanicoAsignadoId;
    }

    private String[] splitNombreCompleto(String nombreCompleto) {
        if (!StringUtils.hasText(nombreCompleto)) {
            return new String[] {"", ""};
        }
        String trimmed = nombreCompleto.trim();
        String[] parts = trimmed.split("\\s+", 2);
        String nombre = parts[0];
        String apellido = parts.length > 1 ? parts[1] : "";
        return new String[] {nombre, apellido};
    }

    private String[] splitMarcaModelo(String marcaModelo) {
        if (!StringUtils.hasText(marcaModelo)) {
            return new String[] {"", ""};
        }
        String trimmed = marcaModelo.trim();
        String[] parts = trimmed.split("\\s+", 2);
        String marca = parts[0];
        String modelo = parts.length > 1 ? parts[1] : "";
        return new String[] {marca, modelo};
    }

    /**
     * Resolves an order identifier (UUID or external_id) to UUID.
     * Tries UUID first, then falls back to external_id lookup.
     *
     * @param id order identifier (UUID or external_id)
     * @return order UUID
     * @throws IllegalArgumentException if order not found
     */
    @TenantOperation
    public UUID resolveOrdenId(String id) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        Orden orden;
        
        // Try to parse as UUID first
        try {
            UUID uuidId = UUID.fromString(id);
            orden = ordenRepository.findByIdAndSucursalId(uuidId, sucursalId).orElse(null);
            if (orden != null) {
                return uuidId;
            }
        } catch (IllegalArgumentException e) {
            // Not a valid UUID, treat as external_id
        }
        
        // If not found by UUID, try by external_id
        orden = ordenRepository.findByExternalIdAndSucursalId(id, sucursalId).orElse(null);
        if (orden != null) {
            return orden.getId();
        }
        
        throw new IllegalArgumentException("Orden no encontrada");
    }

    private OrdenResult toResult(Orden orden) {
        OrdenResult.OrdenResultBuilder builder = OrdenResult.builder()
                .id(orden.getId())
                .externalId(orden.getExternalId())
                .estado(orden.getEstado())
                .tipo(orden.getTipo())
                .diagnosticoInicial(orden.getDiagnosticoInicial())
                .fechaIngreso(orden.getFechaIngreso())
                .fechaPrometida(orden.getFechaPrometida());

        if (orden.getBicicletaId() != null) {
            bicicletaRepository.findById(orden.getBicicletaId()).ifPresent(bicicleta -> {
                builder.bicicletaMarca(bicicleta.getMarca());
                builder.bicicletaModelo(bicicleta.getModelo());
                builder.bicicletaTipo(bicicleta.getTipo());
                builder.bicicletaColor(bicicleta.getColor());
                builder.bicicletaTalla(bicicleta.getAro());

                if (bicicleta.getCliente() != null) {
                    builder.clienteNombre(bicicleta.getCliente().getNombre());
                    builder.clienteApellido(bicicleta.getCliente().getApellido());
                    builder.clienteTelefono(bicicleta.getCliente().getTelefono());
                }
            });
        }

        if (orden.getMecanicoId() != null) {
            usuarioRepository.findById(orden.getMecanicoId()).ifPresent(mecanico -> {
                builder.mecanicoNombre(mecanico.getNombre());
                builder.mecanicoApellido(mecanico.getApellido());
            });
        }

        return builder.build();
    }
}