package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.CompraRequest;
import com.bikeshop.manager.domain.tenant.Compra;
import com.bikeshop.manager.domain.tenant.CompraProducto;
import com.bikeshop.manager.domain.tenant.MovimientoStock;
import com.bikeshop.manager.domain.tenant.Producto;
import com.bikeshop.manager.infrastructure.persistence.repository.CompraRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.MovimientoStockRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.ProductoRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.TenantContext;
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
    private final MovimientoStockRepository movimientoRepository;

    /**
     * Creates a purchase in pending state.
     *
     * @param request purchase request
     * @return created purchase
     */
    @TenantOperation
    @Transactional
    public Compra crear(CompraRequest request) {
        UUID tallerId = TenantContext.getCurrentTenant();
        UUID usuarioId = TenantContext.getCurrentUser();
        if (tallerId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de taller/usuario requerido");
        }

        BigDecimal neto = BigDecimal.ZERO;
        for (var linea : request.getLineas()) {
            BigDecimal subtotal = linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()));
            neto = neto.add(subtotal);
        }
        BigDecimal iva = neto.multiply(IVA_RATE);
        BigDecimal total = neto.add(iva);

        Compra compra = Compra.builder()
                .tallerId(tallerId)
                .proveedorId(request.getProveedorId())
                .usuarioId(usuarioId)
                .numeroFactura(request.getNumeroFactura())
                .neto(neto)
                .iva(iva)
                .total(total)
                .estado("Pendiente")
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

        return compraRepository.save(compra);
    }

    /**
     * Confirms purchase reception and updates stock.
     *
     * @param compraId purchase identifier
     * @return updated purchase
     */
    @TenantOperation
    @Transactional
    public Compra confirmarRecepcion(UUID compraId) {
        UUID tallerId = TenantContext.getCurrentTenant();
        UUID usuarioId = TenantContext.getCurrentUser();
        if (tallerId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de taller/usuario requerido");
        }

        Compra compra = compraRepository.findByIdAndTallerId(compraId, tallerId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));

        if (!"Pendiente".equals(compra.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden recibir compras en estado Pendiente");
        }

        compra.setEstado("Recibida");
        compra.setFechaRecepcion(LocalDate.now());

        for (CompraProducto linea : compra.getLineas()) {
            Producto producto = productoRepository.findByIdAndTallerId(linea.getProductoId(), tallerId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + linea.getProductoId()));

            int stockAnterior = producto.getStockActual();
            int stockNuevo = stockAnterior + linea.getCantidad();
            if (stockNuevo < 0) {
                throw new IllegalStateException("Stock negativo no permitido (RN17)");
            }

            producto.setStockActual(stockNuevo);
            productoRepository.save(producto);

            MovimientoStock movimiento = MovimientoStock.builder()
                    .tallerId(tallerId)
                    .productoId(producto.getId())
                    .compraId(compra.getId())
                    .usuarioId(usuarioId)
                    .tipo("ingreso")
                    .cantidad(linea.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockPosterior(stockNuevo)
                    .motivo("Recepcion de compra #" + compra.getNumeroFactura())
                    .build();
            movimientoRepository.save(movimiento);
        }

        return compraRepository.save(compra);
    }

    /**
     * Lists purchases for the current tenant.
     *
     * @return purchases
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<Compra> listar() {
        UUID tallerId = TenantContext.getCurrentTenant();
        if (tallerId == null) {
            return List.of();
        }
        return compraRepository.findAllByTallerIdOrderByFechaCompraDesc(tallerId);
    }
}
