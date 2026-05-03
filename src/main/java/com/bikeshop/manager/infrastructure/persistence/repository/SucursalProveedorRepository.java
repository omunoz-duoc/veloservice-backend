package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.SucursalProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for branch-supplier links.
 */
@Repository
public interface SucursalProveedorRepository extends JpaRepository<SucursalProveedor, UUID> {
    /**
     * Lists all provider links for a branch.
     *
     * @param sucursalId branch identifier
     * @return branch supplier links
     */
    List<SucursalProveedor> findBySucursalId(UUID sucursalId);

    /**
     * Finds a branch-supplier link.
     *
     * @param sucursalId branch identifier
     * @param proveedorId supplier identifier
     * @return link, if present
     */
    Optional<SucursalProveedor> findBySucursalIdAndProveedorId(UUID sucursalId, UUID proveedorId);

    /**
     * Lists all active branch-supplier links for a branch.
     *
     * @param sucursalId branch identifier
     * @return branch supplier links
     */
    List<SucursalProveedor> findAllBySucursalIdAndActivoTrue(UUID sucursalId);

    /**
     * Checks if a branch-supplier link exists.
     *
     * @param sucursalId branch identifier
     * @param proveedorId supplier identifier
     * @return true if the link exists
     */
    boolean existsBySucursalIdAndProveedorId(UUID sucursalId, UUID proveedorId);
}
