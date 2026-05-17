    /**
     * Lista órdenes por sucursal y mecánico en orden descendente de ingreso.
     */
    List<Orden> findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(UUID sucursalId, UUID mecanicoId);
package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.veloservice.ordenes.domain.model.Orden;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Repository for work orders.
 */
@Repository
public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    Optional<Orden> findByIdAndSucursalId(UUID id, UUID sucursalId);

    List<Orden> findAllBySucursalIdOrderByFechaIngresoDesc(UUID sucursalId);

    boolean existsByNumeroOrdenAndSucursalId(String numeroOrden, UUID sucursalId);
}

/**
 * Finds active orders by mecanico.
 */
List<Orden> findByMecanicoIdAndEstadoNotIn(UUID mecanicoId, List<EstadoOrdenEnum> estados);
