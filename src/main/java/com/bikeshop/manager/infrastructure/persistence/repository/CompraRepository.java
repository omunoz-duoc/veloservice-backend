package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for purchases.
 */
@Repository
public interface CompraRepository extends JpaRepository<Compra, UUID> {
    /**
     * Finds purchases for a branch ordered by purchase date.
     *
     * @param sucursalId branch identifier
     * @return branch purchases
     */
    @Query("select c from Compra c join SucursalProveedor sp on sp.id = c.sucursalProveedorId where sp.sucursalId = :sucursalId order by c.fechaCompra desc")
    List<Compra> findBySucursalIdOrderByFechaCompraDesc(UUID sucursalId);

    /**
     * Lists purchases for a tenant ordered by purchase date.
     *
     * @param tallerId tenant identifier
     * @return tenant purchases
     */
    List<Compra> findAllBySucursalProveedorIdOrderByFechaCompraDesc(UUID sucursalProveedorId);

    /**
     * Finds a purchase by identifier and tenant.
     *
     * @param id purchase identifier
     * @param tallerId tenant identifier
     * @return matching purchase, if present
     */
    Optional<Compra> findById(UUID id);
}
