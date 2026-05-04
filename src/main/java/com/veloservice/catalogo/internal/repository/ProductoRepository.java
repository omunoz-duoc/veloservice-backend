package com.veloservice.catalogo.internal.repository;

import com.veloservice.catalogo.internal.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for products.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, UUID> {
    /**
     * Lists products for a branch.
     *
     * @param sucursalId branch identifier
     * @return branch products
     */
    List<Producto> findBySucursalId(UUID sucursalId);

    /**
     * Finds a product by identifier.
     *
     * @param id product identifier
     * @return matching product, if present
     */
    Optional<Producto> findById(UUID id);

    /**
     * Checks if a SKU already exists for a branch.
     *
     * @param sku product SKU
     * @param sucursalId branch identifier
     * @return true when the SKU exists
     */
    boolean existsBySkuAndSucursalId(String sku, UUID sucursalId);

    /**
     * Lists products for a tenant.
     *
     * @param tallerId tenant identifier
     * @return tenant products
     */
    List<Producto> findAllBySucursalId(UUID sucursalId);

    /**
     * Finds a product by identifier and tenant.
     *
     * @param id product identifier
     * @param tallerId tenant identifier
     * @return matching product, if present
     */
    Optional<Producto> findByIdAndSucursalId(UUID id, UUID sucursalId);

    /**
     * Checks if a SKU already exists for a tenant.
     *
     * @param tallerId tenant identifier
     * @param sku product SKU
     * @return true when the SKU exists
     */
    boolean existsBySucursalIdAndSku(UUID sucursalId, String sku);

    /**
     * Lists products with stock below or equal to minimum.
     *
     * @param tallerId tenant identifier
     * @return low stock products
     */
    @Query("select p from Producto p where p.sucursalId = :sucursalId and p.stock <= p.stockMinimo")
    List<Producto> findAlertasStock(UUID sucursalId);

    /**
     * Lists products under or equal to minimum stock for a branch.
     *
     * @param sucursalId branch identifier
     * @return low stock products
     */
    @Query("select p from Producto p where p.sucursalId = :sucursalId and p.stock <= p.stockMinimo")
    List<Producto> findBySucursalIdAndStockLessThanEqualStockMinimo(UUID sucursalId);
}