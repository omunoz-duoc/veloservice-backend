package com.veloservice.proveedores_compras.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.proveedores_compras.domain.model.SucursalProveedor;

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

    @Query("select count(sp) from SucursalProveedor sp join Sucursal s on s.id = sp.sucursalId where s.tallerId = :tallerId")
    long countByTallerId(@Param("tallerId") UUID tallerId);

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
