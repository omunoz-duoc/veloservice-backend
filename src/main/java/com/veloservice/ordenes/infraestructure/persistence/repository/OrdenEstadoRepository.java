package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.domain.model.OrdenEstado;

import java.util.List;
import java.util.UUID;

/**
 * Repository for order state audit entries.
 */
@Repository
public interface OrdenEstadoRepository extends JpaRepository<OrdenEstado, UUID> {
    /**
     * Lists audit entries for an order.
     *
     * @param ordenId work order identifier
     * @return ordered audit entries
     */
    List<OrdenEstado> findByOrdenIdOrderByCreatedAtAsc(UUID ordenId);
}