package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.CompraRequest;
import com.bikeshop.manager.application.dto.CompraLineaResponse;
import com.bikeshop.manager.domain.tenant.Compra;
import com.bikeshop.manager.domain.tenant.CompraProducto;
import com.bikeshop.manager.domain.tenant.MovimientoStock;
import com.bikeshop.manager.domain.tenant.Producto;
import com.bikeshop.manager.domain.tenant.Proveedor;
import com.bikeshop.manager.domain.tenant.SucursalProveedor;
import com.bikeshop.manager.infrastructure.persistence.repository.CompraRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.MovimientoStockRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.ProductoRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.ProveedorRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.SucursalProveedorRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Handles purchase operations and stock updates.
 */
@Service
@RequiredArgsConstructor
public class CompraService {

    private static final BigDecimal IVA_RATE = BigDecimal.valueOf(0.19);

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final SucursalProveedorRepository sucursalProveedorRepository;
    private final MovimientoStockRepository movimientoRepository;

    /**
     * Creates a purchase in pending state.
     *
     * @param request purchase request
     * @return created purchase
     */
    @TenantOperation
    @Transactional
    public CompraResponse crear(CompraRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = com.bikeshop.manager.infrastructure.security.TenantContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de sucursal/usuario requerido");
        }

        SucursalProveedor sucursalProveedor = sucursalProveedorRepository
                .findById(request.getSucursalProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("SucursalProveedor no encontrado"));
        if (!sucursalId.equals(sucursalProveedor.getSucursalId())) {
            throw new IllegalArgumentException("El proveedor no pertenece a la sucursal actual");
        }

        BigDecimal neto = BigDecimal.ZERO;
        for (var linea : request.getLineas()) {
            BigDecimal subtotal = linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()));
            neto = neto.add(subtotal);
        }
        BigDecimal iva = neto.multiply(IVA_RATE);
        BigDecimal total = neto.add(iva);

        Compra compra = Compra.builder()
            .sucursalProveedorId(request.getSucursalProveedorId())
                .usuarioId(usuarioId)
                .numeroFactura(request.getNumeroFactura())
                .neto(neto)
                .iva(iva)
                .total(total)
                .estado("borrador")
                .fechaCompra(request.getFechaCompra())
                .notas(request.getNotas())
                .build();

        for (var lineaReq : request.getLineas()) {
            BigDecimal subtotal = lineaReq.getPrecioUnitario().multiply(BigDecimal.valueOf(lineaReq.getCantidad()));
            CompraProducto linea = CompraProducto.builder()
                    .compra(compra)
                    .productoId(lineaReq.getProductoId())
                    .cantidad(lineaReq.getCantidad())
                    .precioUnitario(lineaReq.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();
            compra.getLineas().add(linea);
        }

        compra = compraRepository.save(compra);
        return toResponse(compra);
    }

    /**
     * Confirms purchase reception and updates stock.
     *
     * @param compraId purchase identifier
     * @return updated purchase
     */
    @TenantOperation
    @Transactional
    public CompraResponse confirmarRecepcion(UUID compraId) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = com.bikeshop.manager.infrastructure.security.TenantContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de sucursal/usuario requerido");
        }

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));

        SucursalProveedor sucursalProveedor = sucursalProveedorRepository.findById(compra.getSucursalProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor de sucursal no encontrado"));
        if (!sucursalId.equals(sucursalProveedor.getSucursalId())) {
            throw new IllegalArgumentException("La compra no pertenece a la sucursal actual");
        }

        if (!"borrador".equals(compra.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden recepcionar compras en estado borrador");
        }

        compra.setEstado("recibida");
        compra.setFechaRecepcion(LocalDate.now());

        for (CompraProducto linea : compra.getLineas()) {
            Producto producto = productoRepository.findById(linea.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + linea.getProductoId()));
            if (!sucursalId.equals(producto.getSucursalId())) {
                throw new IllegalArgumentException("El producto no pertenece a la sucursal actual");
            }

            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior + linea.getCantidad();
            if (stockNuevo < 0) {
                throw new IllegalStateException("Stock negativo no permitido (RN17)");
            }

            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            MovimientoStock movimiento = MovimientoStock.builder()
                    .productoId(producto.getId())
                    .compraId(compra.getId())
                    .usuarioId(usuarioId)
                    .tipo("entrada")
                    .cantidad(linea.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockPosterior(stockNuevo)
                    .motivo("Recepcion de compra #" + compra.getNumeroFactura())
                    .build();
            movimientoRepository.save(movimiento);
        }

        compra = compraRepository.save(compra);
        return toResponse(compra);
    }

    /**
     * Lists purchases for the current tenant.
     *
     * @return purchases
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<CompraResponse> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return compraRepository.findBySucursalIdOrderByFechaCompraDesc(sucursalId).stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    private CompraResponse toResponse(Compra compra) {
        String proveedorNombre = sucursalProveedorRepository.findById(compra.getSucursalProveedorId())
            .flatMap(sp -> proveedorRepository.findById(sp.getProveedorId()))
            .map(Proveedor::getNombre)
            .orElse(null);

        return CompraResponse.builder()
                .id(compra.getId())
                .proveedorNombre(proveedorNombre)
                .numeroFactura(compra.getNumeroFactura())
                .neto(compra.getNeto())
                .iva(compra.getIva())
                .total(compra.getTotal())
                .estado(compra.getEstado())
                .fechaCompra(compra.getFechaCompra())
                .lineas(compra.getLineas().stream()
                        .map(linea -> CompraLineaResponse.builder()
                                .productoId(linea.getProductoId())
                                .cantidad(linea.getCantidad())
                                .precioUnitario(linea.getPrecioUnitario())
                                .subtotal(linea.getSubtotal())
                                .build())
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    public static class CompraResponse {
        private UUID id;
        private String proveedorNombre;
        private String numeroFactura;
        private BigDecimal neto;
        private BigDecimal iva;
        private BigDecimal total;
        private String estado;
        private LocalDate fechaCompra;
        private List<CompraLineaResponse> lineas;
    }
}
