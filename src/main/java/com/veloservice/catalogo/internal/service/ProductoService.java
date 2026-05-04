package com.veloservice.catalogo.internal.service;

import com.veloservice.catalogo.api.ProductoRequest;
import com.veloservice.catalogo.api.ProductoResponse;
import com.veloservice.inventario.internal.entity.MovimientoStock;
import com.veloservice.catalogo.internal.entity.Producto;
import com.veloservice.inventario.internal.repository.MovimientoStockRepository;
import com.veloservice.catalogo.internal.repository.ProductoRepository;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.config.security.SucursalContext;
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

    @TenantOperation
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            throw new IllegalStateException("Contexto de sucursal requerido");
        }

        int stock = request.getStock() == null ? 0 : request.getStock();
        int stockMinimo = request.getStockMinimo() == null ? 0 : request.getStockMinimo();
        if (stock < 0 || stockMinimo < 0) {
            throw new IllegalArgumentException("Stock negativo no permitido (RN17)");
        }

        if (productoRepository.existsBySkuAndSucursalId(request.getSku(), sucursalId)) {
            throw new IllegalArgumentException("SKU duplicado en esta sucursal: " + request.getSku());
        }

        Producto producto = Producto.builder()
                .sucursalId(sucursalId)
                .categoriaId(request.getCategoriaId())
                .nombre(request.getNombre())
                .sku(request.getSku())
                .marca(request.getMarca())
                .unidadMedida(request.getUnidadMedida())
                .precioCosto(request.getPrecioCosto())
                .precioVenta(request.getPrecioVenta())
                .stock(stock)
                .stockMinimo(stockMinimo)
                .build();

        producto = productoRepository.save(producto);

        return toResponse(producto);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return productoRepository.findBySucursalId(sucursalId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResponse> alertasStockBajo() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return productoRepository.findBySucursalIdAndStockLessThanEqualStockMinimo(sucursalId).stream()
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
                .stock(producto.getStock())
                .stockMinimo(producto.getStockMinimo())
                .alertaStockBajo(producto.getStock() <= producto.getStockMinimo())
                .build();
    }
}