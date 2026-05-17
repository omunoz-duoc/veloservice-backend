package com.veloservice.ordenes.application.usecase;

import com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.ordenes.application.dto.MultimediaCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.domain.model.Multimedia;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenEstado;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenEstadoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.enums.TipoMovimientoEnum;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.UsuarioContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
            EnumSet.of(EstadoOrdenEnum.en_diagnostico, EstadoOrdenEnum.cancelada));
        transiciones.put(EstadoOrdenEnum.en_diagnostico,
            EnumSet.of(EstadoOrdenEnum.esperando_repuestos, EstadoOrdenEnum.en_reparacion, EstadoOrdenEnum.cancelada));
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

    /**
     * Creates a work order with mandatory multimedia evidence.
     *
     * @param request work order request
     * @return created work order response
     */
    @TenantOperation
    @Transactional
    public OrdenResult crear(OrdenCreateCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de sucursal/usuario requerido");
        }

        if (command.getMultimedia() == null || command.getMultimedia().isEmpty()) {
            throw new IllegalArgumentException("Evidencia multimedia obligatoria (RN01)");
        }

        String numeroOrden = secuenciaService.generarNumeroOrden(sucursalId);

        Orden orden = Orden.builder()
            .sucursalId(sucursalId)
                .bicicletaId(command.getBicicletaId())
                .mecanicoId(usuarioId)
                .numeroOrden(numeroOrden)
            .estado(EstadoOrdenEnum.recibida)
                .tipo(command.getTipo())
                .diagnosticoInicial(command.getDiagnosticoInicial())
                .observacionesCliente(command.getObservacionesCliente())
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(OffsetDateTime.now())
                .build();

        orden = ordenRepository.save(orden);

        for (MultimediaCreateCommand m : command.getMultimedia()) {
            Multimedia multimedia = Multimedia.builder()
                    .ordenId(orden.getId())
                    .usuarioId(usuarioId)
                    .url(m.getUrl())
                    .tipoArchivo(m.getTipoArchivo())
                    .etapa(EtapaMultimediaEnum.ingreso)
                    .descripcion(m.getDescripcion())
                    .build();
            multimediaRepository.save(multimedia);
        }

        registrarEstado(orden.getId(), sucursalId, usuarioId, null, EstadoOrdenEnum.recibida,
                "Creacion de orden de trabajo");

        return toResult(orden);
    }

    /**
     * Changes the state of a work order and records audit evidence.
     *
     * @param ordenId work order identifier
     * @param request change request
     * @return updated work order response
     */
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
                        "No se puede cambiar a lista_para_entrega sin evidencia tecnica final (RN02)");
            }
        }

        if (EstadoOrdenEnum.cancelada.equals(estadoNuevo)
                && EnumSet.of(EstadoOrdenEnum.lista_para_entrega, EstadoOrdenEnum.entregada).contains(estadoActual)) {
            throw new IllegalArgumentException("No se puede cancelar una orden en estado " + estadoActual);
        }

        orden.setEstado(estadoNuevo);
        if (EstadoOrdenEnum.entregada.equals(estadoNuevo)) {
            orden.setFechaEntrega(OffsetDateTime.now());
        }
        orden = ordenRepository.save(orden);

        registrarEstado(ordenId, sucursalId, usuarioId, estadoActual, estadoNuevo, command.getObservacion());

        return toResult(orden);
    }

        /**
         * Agrega un servicio a la orden tomando snapshot de precios.
         */
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
            .descuentoAplicado(java.math.BigDecimal.ZERO)
            .notas(command.getNotas())
            .build();

        ordenServicioRepository.save(ordenServicio);

        return toResult(orden);
        }

        /**
         * Agrega un producto a la orden, valida stock y registra movimiento tipo "salida".
         */
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
            .precioAplicado(producto.getPrecioVenta().multiply(new java.math.BigDecimal(command.getCantidad())))
            .proporcionadoPorCliente(Boolean.TRUE.equals(command.getProporcionadoPorCliente()))
            .notas(command.getNotas())
            .build();

        ordenProductoRepository.save(ordenProducto);

        return toResult(orden);
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

    /**
     * Retrieves a work order by identifier.
     *
     * @param id work order identifier
     * @return work order response
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenResult obtener(UUID id) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        Orden orden = ordenRepository.findByIdAndSucursalId(id, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        return toResult(orden);
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

    private OrdenResult toResult(Orden orden) {
        return OrdenResult.builder()
                .id(orden.getId())
                .numeroOrden(orden.getNumeroOrden())
                .estado(orden.getEstado())
                .tipo(orden.getTipo())
                .bicicletaId(orden.getBicicletaId())
                .mecanicoId(orden.getMecanicoId())
                .diagnosticoInicial(orden.getDiagnosticoInicial())
                .fechaIngreso(orden.getFechaIngreso())
                .fechaPrometida(orden.getFechaPrometida())
                .build();
    }
}