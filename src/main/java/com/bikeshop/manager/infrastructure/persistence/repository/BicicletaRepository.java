package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Bicicleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tenant bikes.
 */
@Repository
public interface BicicletaRepository extends JpaRepository<Bicicleta, UUID> {
    /**
     * Lists bikes for a customer.
     *
     * @param clienteId customer identifier
     * @return customer bikes
     */
    List<Bicicleta> findByClienteId(UUID clienteId);

    /**
     * Finds a bike by identifier and tenant.
     *
     * @param id bike identifier
     * @param tallerId tenant identifier
     * @return matching bike, if present
     */
    Optional<Bicicleta> findByIdAndTallerId(UUID id, UUID tallerId);

    /**
     * Lists all bikes for a tenant.
     *
     * @param tallerId tenant identifier
     * @return tenant bikes
     */
    List<Bicicleta> findAllByTallerId(UUID tallerId);
}
