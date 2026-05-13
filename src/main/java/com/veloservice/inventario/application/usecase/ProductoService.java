package com.veloservice.inventario.application.usecase;

import com.veloservice.inventario.application.dto.ProductoCreateCommand;
import com.veloservice.inventario.application.dto.ProductoResult;
import com.veloservice.inventario.application.exception.ProductoErrorCode;
import com.veloservice.inventario.application.exception.ProductoException;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.inventario.interfaces.mapper.ProductoMapper;
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

    @TenantOperation
    @Transactional
    @SuppressWarnings("null")
    public ProductoResult crear(ProductoCreateCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        validateSucursal(sucursalId);

        int stock = command.getStock() == null ? 0 : command.getStock();
        int stockMinimo = command.getStockMinimo() == null ? 0 : command.getStockMinimo();
        validateStock(stock, stockMinimo);

        validateSkuUnico(command.getSku(), sucursalId);

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

        return ProductoMapper.toResult(producto);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return productoRepository.findBySucursalId(sucursalId).stream()
                .map(ProductoMapper::toResult)
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
                .map(ProductoMapper::toResult)
                .collect(Collectors.toList());
    }

    private void validateSucursal(UUID sucursalId) {
        if (sucursalId == null) {
            throw new ProductoException(ProductoErrorCode.SUCURSAL_REQUERIDA, "Contexto de sucursal requerido");
        }
    }

    private void validateStock(Integer stock, Integer stockMinimo) {
        if (stock < 0 || stockMinimo < 0) {
            throw new ProductoException(ProductoErrorCode.STOCK_NEGATIVO, "Stock negativo no permitido (RN17)");
        }
    }

    private void validateSkuUnico(String sku, UUID sucursalId) {
        if (productoRepository.existsBySkuAndSucursalId(sku, sucursalId)) {
            throw new ProductoException(ProductoErrorCode.SKU_DUPLICADO, "SKU duplicado en esta sucursal: " + sku);
        }
    }
}