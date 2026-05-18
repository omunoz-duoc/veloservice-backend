package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.domain.model.OrdenProducto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for work order products.
 */
@Repository
public interface OrdenProductoRepository extends JpaRepository<OrdenProducto, UUID> {
    /**
     * Lists product lines for a work order.
     *
     * @param ordenId work order identifier
     * @return work order products
     */
    List<OrdenProducto> findByOrdenId(UUID ordenId);

    /**
     * Finds a product line by work order and catalog product identifiers.
     *
     * @param ordenId    work order identifier
     * @param productoId catalog product identifier
     * @return matching order product, if present
     */
    Optional<OrdenProducto> findByOrdenIdAndProductoId(UUID ordenId, UUID productoId);
}