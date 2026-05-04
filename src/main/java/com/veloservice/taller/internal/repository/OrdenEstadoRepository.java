package com.veloservice.taller.internal.repository;

import com.veloservice.taller.internal.entity.OrdenEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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