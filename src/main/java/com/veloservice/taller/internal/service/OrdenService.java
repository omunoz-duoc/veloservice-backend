package com.veloservice.taller.internal.service;

import com.veloservice.taller.api.EstadoChangeRequest;
import com.veloservice.taller.api.MultimediaRequest;
import com.veloservice.taller.api.OrdenRequest;
import com.veloservice.taller.api.OrdenResponse;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.enums.TipoMovimientoEnum;
import com.veloservice.taller.internal.entity.Multimedia;
import com.veloservice.taller.internal.entity.Orden;
import com.veloservice.taller.internal.entity.OrdenEstado;
import com.veloservice.taller.internal.repository.MultimediaRepository;
import com.veloservice.taller.internal.repository.OrdenProductoRepository;
import com.veloservice.taller.internal.repository.OrdenServicioRepository;
import com.veloservice.catalogo.internal.repository.ProductoRepository;
import com.veloservice.catalogo.internal.repository.ServicioRepository;
import com.veloservice.inventario.internal.repository.MovimientoStockRepository;
import com.veloservice.taller.internal.repository.OrdenEstadoRepository;
import com.veloservice.taller.internal.repository.OrdenRepository;
import com.veloservice.config.tenant.TenantOperation;
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
    public OrdenResponse crear(OrdenRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de sucursal/usuario requerido");
        }

        if (request.getMultimedia() == null || request.getMultimedia().isEmpty()) {
            throw new IllegalArgumentException("Evidencia multimedia obligatoria (RN01)");
        }

        String numeroOrden = secuenciaService.generarNumeroOrden(sucursalId);

        Orden orden = Orden.builder()
            .sucursalId(sucursalId)
                .bicicletaId(request.getBicicletaId())
                .mecanicoId(usuarioId)
                .numeroOrden(numeroOrden)
            .estado(EstadoOrdenEnum.recibida)
                .tipo(request.getTipo())
                .diagnosticoInicial(request.getDiagnosticoInicial())
                .observacionesCliente(request.getObservacionesCliente())
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .fechaIngreso(OffsetDateTime.now())
                .build();

        orden = ordenRepository.save(orden);

        for (MultimediaRequest m : request.getMultimedia()) {
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

        return toResponse(orden);
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
    public OrdenResponse cambiarEstado(UUID ordenId, EstadoChangeRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();

        Orden orden = ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        EstadoOrdenEnum estadoActual = orden.getEstado();
        EstadoOrdenEnum estadoNuevo = request.getNuevoEstado();

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

        registrarEstado(ordenId, sucursalId, usuarioId, estadoActual, estadoNuevo, request.getObservacion());

        return toResponse(orden);
    }

        /**
         * Agrega un servicio a la orden tomando snapshot de precios.
         */
        @TenantOperation
        @Transactional
        public OrdenResponse agregarServicio(UUID ordenId, com.veloservice.taller.api.OrdenServicioRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();

        Orden orden = ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
            .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        var servicio = servicioRepository.findById(request.getServicioId())
            .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        var ordenServicio = com.veloservice.taller.internal.entity.OrdenServicio.builder()
            .ordenId(ordenId)
            .servicioId(servicio.getId())
            .precioBaseSnapshot(servicio.getPrecioBase())
            .precioAplicado(servicio.getPrecioBase())
            .descuentoAplicado(java.math.BigDecimal.ZERO)
            .notas(request.getNotas())
            .build();

        ordenServicioRepository.save(ordenServicio);

        return toResponse(orden);
        }

        /**
         * Agrega un producto a la orden, valida stock y registra movimiento tipo "salida".
         */
        @TenantOperation
        @Transactional
        public OrdenResponse agregarProducto(UUID ordenId, com.veloservice.taller.api.OrdenProductoRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();

        Orden orden = ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
            .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        var producto = productoRepository.findByIdAndSucursalId(request.getProductoId(), sucursalId)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado en la sucursal"));

        if (!Boolean.TRUE.equals(request.getProporcionadoPorCliente())) {
            if (producto.getStock() < request.getCantidad()) {
            throw new IllegalArgumentException("Stock insuficiente para el producto solicitado");
            }

            int stockAnterior = producto.getStock();
            producto.setStock(stockAnterior - request.getCantidad());
            productoRepository.save(producto);

            var movimiento = com.veloservice.inventario.internal.entity.MovimientoStock.builder()
                .productoId(producto.getId())
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .tipo(TipoMovimientoEnum.salida)
                .cantidad(request.getCantidad())
                .stockAnterior(stockAnterior)
                .stockPosterior(producto.getStock())
                .motivo("Consumo por orden de trabajo")
                .build();
            movimientoStockRepository.save(movimiento);
        }

        var ordenProducto = com.veloservice.taller.internal.entity.OrdenProducto.builder()
            .ordenId(ordenId)
            .productoId(producto.getId())
            .cantidad(request.getCantidad())
            .precioCostoSnapshot(producto.getPrecioCosto())
            .precioVentaSnapshot(producto.getPrecioVenta())
            .precioAplicado(producto.getPrecioVenta().multiply(new java.math.BigDecimal(request.getCantidad())))
            .proporcionadoPorCliente(Boolean.TRUE.equals(request.getProporcionadoPorCliente()))
            .notas(request.getNotas())
            .build();

        ordenProductoRepository.save(ordenProducto);

        return toResponse(orden);
        }

    /**
     * Lists work orders for the current tenant.
     *
     * @return work orders
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenResponse> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        return ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId).stream()
                .map(this::toResponse)
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
    public OrdenResponse obtener(UUID id) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        Orden orden = ordenRepository.findByIdAndSucursalId(id, sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        return toResponse(orden);
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

    private OrdenResponse toResponse(Orden orden) {
        return OrdenResponse.builder()
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