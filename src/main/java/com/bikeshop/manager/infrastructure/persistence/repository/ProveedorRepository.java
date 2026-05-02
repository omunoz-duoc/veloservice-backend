package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for suppliers.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, UUID> {
    /**
     * Lists suppliers for a tenant.
     *
     * @param tallerId tenant identifier
     * @return tenant suppliers
     */
    List<Proveedor> findAllByTallerId(UUID tallerId);

    /**
     * Finds a supplier by identifier and tenant.
     *
     * @param id supplier identifier
     * @param tallerId tenant identifier
     * @return matching supplier, if present
     */
    Optional<Proveedor> findByIdAndTallerId(UUID id, UUID tallerId);
}
