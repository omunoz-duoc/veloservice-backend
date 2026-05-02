package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Lists purchases for a tenant ordered by purchase date.
     *
     * @param tallerId tenant identifier
     * @return tenant purchases
     */
    List<Compra> findAllByTallerIdOrderByFechaCompraDesc(UUID tallerId);

    /**
     * Finds a purchase by identifier and tenant.
     *
     * @param id purchase identifier
     * @param tallerId tenant identifier
     * @return matching purchase, if present
     */
    Optional<Compra> findByIdAndTallerId(UUID id, UUID tallerId);
}
