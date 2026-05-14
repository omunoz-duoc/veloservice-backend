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

    Optional<Orden> findByIdAndSucursalId(UUID id, UUID sucursalId);

    List<Orden> findAllBySucursalIdOrderByFechaIngresoDesc(UUID sucursalId);

    boolean existsByNumeroOrdenAndSucursalId(String numeroOrden, UUID sucursalId);

    /**
     * Counts orders by bicicleta id list.
     */
    long countByBicicletaIdIn(List<UUID> bicicletaIds);

    /**
     * Finds orders by bicicleta id list.
     */
    List<Orden> findAllByBicicletaIdIn(List<UUID> bicicletaIds);
}