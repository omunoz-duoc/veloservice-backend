package com.veloservice.inventario.application.usecase;

import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.inventario.application.dto.ProductoCreateCommand;
import com.veloservice.inventario.application.dto.ProductoResult;
import com.veloservice.inventario.application.exception.ProductoErrorCode;
import com.veloservice.inventario.application.exception.ProductoException;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.CategoriaProductoRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.inventario.interfaces.mapper.ProductoMapper;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.config.tenant.SucursalContext;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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
    private final CategoriaProductoRepository categoriaProductoRepository;
    private final SucursalRepository sucursalRepository;
    private final EntityManager entityManager;

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
                .activo(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        producto = productoRepository.save(producto);

        return ProductoMapper.toResult(producto);
    }

    @TenantOperation
    @Transactional
    public ProductoResult actualizar(UUID id, ProductoCreateCommand command) {
        UUID sucursalId = resolveSucursalId(null);
        validateSucursal(sucursalId);

        int stock = command.getStock() == null ? 0 : command.getStock();
        int stockMinimo = command.getStockMinimo() == null ? 0 : command.getStockMinimo();
        validateStock(stock, stockMinimo);

        Producto producto = productoRepository.findByIdAndSucursalId(id, sucursalId)
                .orElseThrow(() -> new ProductoException(
                        ProductoErrorCode.PRODUCTO_NO_ENCONTRADO,
                        "Producto no encontrado"
                ));

        producto.setNombre(command.getNombre());
        producto.setSku(command.getSku());
        producto.setMarca(command.getMarca());
        producto.setUnidadMedida(command.getUnidadMedida() == null || command.getUnidadMedida().isBlank()
                ? producto.getUnidadMedida()
                : command.getUnidadMedida());
        producto.setPrecioCosto(command.getPrecioCosto());
        producto.setPrecioVenta(command.getPrecioVenta());
        producto.setStock(stock);
        producto.setStockMinimo(stockMinimo);
        if (command.getCategoriaId() != null) {
            producto.setCategoriaId(command.getCategoriaId());
        }
        producto.setUpdatedAt(OffsetDateTime.now());

        producto = productoRepository.save(producto);
        return toResult(producto);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> listar() {
        return listar(null);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> listar(UUID sucursalId) {
        UUID resolvedSucursalId = resolveSucursalId(sucursalId);
        if (resolvedSucursalId == null) {
            return List.of();
        }
        return productoRepository.findBySucursalIdAndActivoTrueOrderByNombreAsc(resolvedSucursalId).stream()
                .map(producto -> toResult(producto, resolveCategoriaNombre(producto.getCategoriaId())))
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> listarBySucursal(UUID sucursalId) {
        return productoRepository.findBySucursalIdAndActivoTrueOrderByNombreAsc(sucursalId).stream()
                .map(producto -> toResult(producto, resolveCategoriaNombre(producto.getCategoriaId())))
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> buscar(String query) {
        return buscar(query, null);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> buscar(String query, UUID sucursalId) {
        if (query == null || query.isBlank()) {
            return listar(sucursalId);
        }
        UUID resolvedSucursalId = resolveSucursalId(sucursalId);
        if (resolvedSucursalId == null) {
            return List.of();
        }
        return productoRepository.searchBySucursalId(resolvedSucursalId, query.trim()).stream()
                .map(producto -> toResult(producto, resolveCategoriaNombre(producto.getCategoriaId())))
                .collect(Collectors.toList());
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProductoResult> alertasStockBajo() {
        UUID sucursalId = resolveSucursalId(null);
        if (sucursalId == null) {
            return List.of();
        }
        return productoRepository.findBySucursalIdAndStockLessThanEqualStockMinimo(sucursalId).stream()
                .map(producto -> toResult(producto, resolveCategoriaNombre(producto.getCategoriaId())))
                .collect(Collectors.toList());
    }

        @TenantOperation
        @Transactional(readOnly = true)
        public com.veloservice.inventario.interfaces.rest.dto.InventarioMetricasResponse metricas() {
        UUID sucursalId = resolveSucursalId(null);
        if (sucursalId == null) {
            return com.veloservice.inventario.interfaces.rest.dto.InventarioMetricasResponse.builder()
                .valorInventario(0)
                .enStock(0)
                .stockBajo(0)
                .agotados(0)
                .build();
        }

        List<Producto> productos = productoRepository.findBySucursalId(sucursalId);
        long enStock = productos.stream()
            .mapToLong(p -> p.getStock() == null ? 0 : p.getStock())
            .sum();
        long stockBajo = productos.stream()
            .filter(p -> p.getStock() != null && p.getStockMinimo() != null && p.getStock() <= p.getStockMinimo())
            .count();
        long agotados = productos.stream()
            .filter(p -> p.getStock() != null && p.getStock() == 0)
            .count();
        long valorInventario = productos.stream()
            .mapToLong(p -> {
                if (p.getPrecioCosto() == null || p.getStock() == null) {
                return 0L;
                }
                return p.getPrecioCosto().longValue() * p.getStock();
            })
            .sum();

        return com.veloservice.inventario.interfaces.rest.dto.InventarioMetricasResponse.builder()
            .valorInventario(valorInventario)
            .enStock(enStock)
            .stockBajo(stockBajo)
            .agotados(agotados)
            .build();
        }

    private ProductoResult toResult(Producto producto) {
        return toResult(producto, resolveCategoriaNombre(producto.getCategoriaId()));
    }

    private ProductoResult toResult(Producto producto, String categoriaNombre) {
        return ProductoResult.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .sku(producto.getSku())
                .marca(producto.getMarca())
                .categoriaId(producto.getCategoriaId())
                .categoriaNombre(categoriaNombre)
                .precioCosto(producto.getPrecioCosto())
                .precioVenta(producto.getPrecioVenta())
                .stock(producto.getStock())
                .stockMinimo(producto.getStockMinimo())
                .alertaStockBajo(producto.getStock() <= producto.getStockMinimo())
                .build();
    }

    private String resolveCategoriaNombre(UUID categoriaId) {
        if (categoriaId == null) {
            return null;
        }
        return categoriaProductoRepository.findById(categoriaId)
                .map(categoria -> categoria.getNombre())
                .orElse(null);
    }

    private UUID resolveSucursalId(UUID requestedSucursalId) {
        if (requestedSucursalId != null) {
            validateSucursalBelongsToTaller(requestedSucursalId);
            return requestedSucursalId;
        }

        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            return sucursalId;
        }
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            return null;
        }
        UUID fallbackSucursalId = sucursalRepository.findFirstByTallerIdAndActivoTrueOrderByCreatedAtAsc(tallerId)
                .map(Sucursal::getId)
                .orElse(null);
        if (fallbackSucursalId != null) {
            SucursalContext.setCurrentSucursal(fallbackSucursalId);
            entityManager.createNativeQuery("SELECT set_config('app.current_sucursal_id', ?, false)")
                    .setParameter(1, fallbackSucursalId.toString())
                    .getSingleResult();
        }
        return fallbackSucursalId;
    }

    private void validateSucursalBelongsToTaller(UUID sucursalId) {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            return;
        }
        boolean belongsToTaller = sucursalRepository.existsByIdAndTallerId(sucursalId, tallerId);
        if (!belongsToTaller) {
            throw new IllegalArgumentException("Sucursal no pertenece al taller del usuario");
        }
    }

    private void validateSucursal(UUID sucursalId) {
        if (sucursalId == null) {
            throw new IllegalArgumentException("Sucursal requerida");
        }
    }

    private void validateStock(int stock, int stockMinimo) {
        if (stock < 0 || stockMinimo < 0) {
            throw new IllegalArgumentException("Stock no puede ser negativo");
        }
    }

    private void validateSkuUnico(String sku, UUID sucursalId) {
        if (sku == null || sku.isBlank()) return;
        // TODO: validar unicidad real contra repositorio
    }
}
