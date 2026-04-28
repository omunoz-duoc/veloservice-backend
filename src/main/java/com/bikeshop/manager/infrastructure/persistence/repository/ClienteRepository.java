package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tenant customers.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    /**
     * Finds a customer by identifier and tenant.
     *
     * @param id customer identifier
     * @param tallerId tenant identifier
     * @return matching customer, if present
     */
    Optional<Cliente> findByIdAndTallerId(UUID id, UUID tallerId);

    /**
     * Lists all customers for a tenant.
     *
     * @param tallerId tenant identifier
     * @return tenant customers
     */
    List<Cliente> findAllByTallerId(UUID tallerId);

    /**
     * Checks if a RUT exists within a tenant.
     *
     * @param tallerId tenant identifier
     * @param rut customer RUT
     * @return true if a customer already uses the RUT
     */
    boolean existsByTallerIdAndRut(UUID tallerId, String rut);
}
