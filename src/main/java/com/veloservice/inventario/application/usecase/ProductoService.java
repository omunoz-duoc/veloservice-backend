package com.veloservice.inventario.application.usecase;

import com.veloservice.inventario.application.dto.ProductoCreateCommand;
import com.veloservice.inventario.application.dto.ProductoResult;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
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
    public ProductoResult crear(ProductoCreateCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            throw new IllegalStateException("Contexto de sucursal requerido");
        }

        int stock = command.getStock() == null ? 0 : command.getStock();
        int stockMinimo = command.getStockMinimo() == null ? 0 : command.getStockMinimo();
        if (stock < 0 || stockMinimo < 0) {
            throw new IllegalArgumentException("Stock negativo no permitido (RN17)");
        }

        if (productoRepository.existsBySkuAndSucursalId(command.getSku(), sucursalId)) {
            throw new IllegalArgumentException("SKU duplicado en esta sucursal: " + command.getSku());
        }

        Producto producto = Producto.builder()
                .sucursalId(sucursalId)
                .categoriaId(command.getCategoriaId())
                .nombre(command.getNombre())
                .sku(command.getSku())
                .marca(command.getMarca())
                .unidadMedida(command.getUnidadMedida())
                .precioCosto(command.getPrecioCosto())
                .precioVenta(command.getPrecioVenta())
                .stock(stock)
                .stockMinimo(stockMinimo)
                .build();

        producto = productoRepository.save(producto);

        return toResult(producto);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return productoRepository.findBySucursalId(sucursalId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> alertasStockBajo() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return productoRepository.findBySucursalIdAndStockLessThanEqualStockMinimo(sucursalId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private ProductoResult toResult(Producto producto) {
        return ProductoResult.builder()
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