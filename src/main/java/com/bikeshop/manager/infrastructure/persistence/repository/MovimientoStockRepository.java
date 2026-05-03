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
    List<MovimientoStock> findByProductoId(UUID productoId);

    /**
     * Lists stock movements for a product ordered by creation date.
     *
     * @param productoId product identifier
     * @return ordered movements
     */
    List<MovimientoStock> findByProductoIdOrderByCreatedAtDesc(UUID productoId);

}
