package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for work orders.
 */
@Repository
public interface OrdenRepository extends JpaRepository<Orden, UUID> {
    /**
     * Finds a work order by identifier and tenant.
     *
     * @param id work order identifier
     * @param tallerId tenant identifier
     * @return matching work order, if present
     */
    Optional<Orden> findByIdAndTallerId(UUID id, UUID tallerId);

    /**
     * Lists work orders for a tenant in descending creation order.
     *
     * @param tallerId tenant identifier
     * @return tenant work orders
     */
    List<Orden> findAllByTallerIdOrderByFechaIngresoDesc(UUID tallerId);

    /**
     * Checks if a sequence is already used for a tenant.
     *
     * @param numeroOrden sequence number
     * @param tallerId tenant identifier
     * @return true if the sequence exists
     */
    boolean existsByNumeroOrdenAndTallerId(String numeroOrden, UUID tallerId);
}
