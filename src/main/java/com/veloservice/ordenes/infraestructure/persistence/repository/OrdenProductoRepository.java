package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.domain.model.OrdenProducto;

import java.util.List;
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
}