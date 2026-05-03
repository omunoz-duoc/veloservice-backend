package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Garantia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for warranties.
 */
@Repository
public interface GarantiaRepository extends JpaRepository<Garantia, UUID> {
    /**
     * Lists warranties for a work order.
     *
     * @param ordenId work order identifier
     * @return warranties
     */
    List<Garantia> findByOrdenId(UUID ordenId);

    /**
     * Finds a warranty by its number.
     *
     * @param numeroGarantia warranty number
     * @return matching warranty, if present
     */
    Optional<Garantia> findByNumeroGarantia(String numeroGarantia);
}
