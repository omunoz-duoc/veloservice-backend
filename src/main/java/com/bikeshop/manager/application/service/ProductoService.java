package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.ProductoRequest;
import com.bikeshop.manager.application.dto.ProductoResponse;
import com.bikeshop.manager.domain.tenant.MovimientoStock;
import com.bikeshop.manager.domain.tenant.Producto;
import com.bikeshop.manager.infrastructure.persistence.repository.MovimientoStockRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.ProductoRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles product operations and stock alerts.
 */
@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoRepository;

    /**
     * Creates a product and registers initial stock movement if needed.
     *
     * @param request product request
     * @return product response
     */
    @TenantOperation
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        UUID tallerId = TenantContext.getCurrentTenant();
        UUID usuarioId = TenantContext.getCurrentUser();
        if (tallerId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de taller/usuario requerido");
        }

        int stockActual = request.getStockActual() == null ? 0 : request.getStockActual();
        int stockMinimo = request.getStockMinimo() == null ? 0 : request.getStockMinimo();
        if (stockActual < 0 || stockMinimo < 0) {
            throw new IllegalArgumentException("Stock negativo no permitido (RN17)");
        }

        if (productoRepository.existsByTallerIdAndSku(tallerId, request.getSku())) {
            throw new IllegalArgumentException("SKU duplicado en este taller: " + request.getSku());
        }

        Producto producto = Producto.builder()
                .tallerId(tallerId)
                .categoriaId(request.getCategoriaId())
                .nombre(request.getNombre())
                .sku(request.getSku())
                .marca(request.getMarca())
                .unidadMedida(request.getUnidadMedida())
                .precioCosto(request.getPrecioCosto())
                .precioVenta(request.getPrecioVenta())
                .stockActual(stockActual)
                .stockMinimo(stockMinimo)
                .build();

        producto = productoRepository.save(producto);

        if (stockActual > 0) {
            MovimientoStock movimiento = MovimientoStock.builder()
                    .tallerId(tallerId)
                    .productoId(producto.getId())
                    .usuarioId(usuarioId)
                    .tipo("ajuste_inicial")
                .cantidad(stockActual)
                    .stockAnterior(0)
                .stockPosterior(stockActual)
                    .motivo("Stock inicial al crear producto")
                    .build();
            movimientoRepository.save(movimiento);
        }

        return toResponse(producto);
    }

    /**
     * Lists products for the current tenant.
     *
     * @return product responses
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        UUID tallerId = TenantContext.getCurrentTenant();
        return productoRepository.findAllByTallerId(tallerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lists products under minimum stock.
     *
     * @return product responses with alerts
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarAlertasStock() {
        UUID tallerId = TenantContext.getCurrentTenant();
        return productoRepository.findAlertasStock(tallerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ProductoResponse toResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .sku(producto.getSku())
                .marca(producto.getMarca())
                .precioCosto(producto.getPrecioCosto())
                .precioVenta(producto.getPrecioVenta())
                .stockActual(producto.getStockActual())
                .stockMinimo(producto.getStockMinimo())
                .alertaStockBajo(producto.getStockActual() <= producto.getStockMinimo())
                .build();
    }
}
