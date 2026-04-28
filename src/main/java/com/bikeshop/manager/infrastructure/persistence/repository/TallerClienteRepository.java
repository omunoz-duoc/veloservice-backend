package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.TallerCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tenant-customer links.
 */
@Repository
public interface TallerClienteRepository extends JpaRepository<TallerCliente, UUID> {
    /**
     * Finds a tenant-customer link.
     *
     * @param tallerId tenant identifier
     * @param clienteId customer identifier
     * @return link, if present
     */
    Optional<TallerCliente> findByTallerIdAndClienteId(UUID tallerId, UUID clienteId);

    /**
     * Checks if a tenant-customer link exists.
     *
     * @param tallerId tenant identifier
     * @param clienteId customer identifier
     * @return true if the link exists
     */
    boolean existsByTallerIdAndClienteId(UUID tallerId, UUID clienteId);
}
