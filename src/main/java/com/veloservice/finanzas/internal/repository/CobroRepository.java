package com.veloservice.finanzas.internal.repository;

import com.veloservice.finanzas.internal.entity.Cobro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for settlements.
 */
@Repository
public interface CobroRepository extends JpaRepository<Cobro, UUID> {
    /**
     * Finds a settlement by work order.
     *
     * @param ordenId work order identifier
     * @return matching settlement, if present
     */
    Optional<Cobro> findByOrdenId(UUID ordenId);
}