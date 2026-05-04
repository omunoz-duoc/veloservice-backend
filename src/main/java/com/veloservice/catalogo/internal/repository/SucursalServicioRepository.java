package com.veloservice.catalogo.internal.repository;

import com.veloservice.catalogo.internal.entity.SucursalServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for branch-service customizations.
 */
@Repository
public interface SucursalServicioRepository extends JpaRepository<SucursalServicio, UUID> {
    /**
     * Finds a branch-service link.
     *
     * @param sucursalId branch identifier
     * @param servicioId service identifier
     * @return link, if present
     */
    Optional<SucursalServicio> findBySucursalIdAndServicioId(UUID sucursalId, UUID servicioId);

    /**
     * Lists all active branch-service links for a branch.
     *
     * @param sucursalId branch identifier
     * @return branch service links
     */
    List<SucursalServicio> findAllBySucursalIdAndActivoTrue(UUID sucursalId);

    /**
     * Checks if a branch-service link exists.
     *
     * @param sucursalId branch identifier
     * @param servicioId service identifier
     * @return true if the link exists
     */
    boolean existsBySucursalIdAndServicioId(UUID sucursalId, UUID servicioId);
}