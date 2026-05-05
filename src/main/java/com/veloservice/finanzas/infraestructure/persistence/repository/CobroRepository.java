package com.veloservice.finanzas.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.finanzas.domain.model.Cobro;

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