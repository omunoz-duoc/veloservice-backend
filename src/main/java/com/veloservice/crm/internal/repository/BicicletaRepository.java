package com.veloservice.crm.internal.repository;

import com.veloservice.crm.internal.entity.Bicicleta;
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
}