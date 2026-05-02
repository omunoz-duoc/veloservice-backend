package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for stock movements.
 */
@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, UUID> {
    /**
     * Lists stock movements for a product.
     *
     * @param productoId product identifier
     * @return movements ordered by creation date
     */
    List<MovimientoStock> findByProductoIdOrderByCreatedAtDesc(UUID productoId);

    /**
     * Lists stock movements for a tenant.
     *
     * @param tallerId tenant identifier
     * @return movements ordered by creation date
     */
    List<MovimientoStock> findByTallerIdOrderByCreatedAtDesc(UUID tallerId);
}
