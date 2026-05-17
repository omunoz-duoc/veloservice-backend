    /**
     * Lista órdenes por sucursal y mecánico en orden descendente de ingreso.
     */
    List<Orden> findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(UUID sucursalId, UUID mecanicoId);
package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.domain.model.Orden;

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
    Optional<Orden> findByIdAndSucursalId(UUID id, UUID sucursalId);

    /**
     * Lists work orders for a tenant in descending creation order.
     *
     * @param tallerId tenant identifier
     * @return tenant work orders
     */
    List<Orden> findAllBySucursalIdOrderByFechaIngresoDesc(UUID sucursalId);

    /**
     * Checks if a sequence is already used for a tenant.
     *
     * @param numeroOrden sequence number
     * @param tallerId tenant identifier
     * @return true if the sequence exists
     */
    boolean existsByNumeroOrdenAndSucursalId(String numeroOrden, UUID sucursalId);
}