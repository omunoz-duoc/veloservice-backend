package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Producto;
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
     * Lists products for a tenant.
     *
     * @param tallerId tenant identifier
     * @return tenant products
     */
    List<Producto> findAllByTallerId(UUID tallerId);

    /**
     * Finds a product by identifier and tenant.
     *
     * @param id product identifier
     * @param tallerId tenant identifier
     * @return matching product, if present
     */
    Optional<Producto> findByIdAndTallerId(UUID id, UUID tallerId);

    /**
     * Checks if a SKU already exists for a tenant.
     *
     * @param tallerId tenant identifier
     * @param sku product SKU
     * @return true when the SKU exists
     */
    boolean existsByTallerIdAndSku(UUID tallerId, String sku);

    /**
     * Lists products with stock below or equal to minimum.
     *
     * @param tallerId tenant identifier
     * @return low stock products
     */
    @Query("select p from Producto p where p.tallerId = :tallerId and p.stockActual <= p.stockMinimo")
    List<Producto> findAlertasStock(UUID tallerId);
}
