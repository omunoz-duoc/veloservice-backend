package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.domain.model.TipoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the order type catalog.
 */
@Repository
public interface TipoOrdenRepository extends JpaRepository<TipoOrden, UUID> {
    Optional<TipoOrden> findByCodigo(String codigo);
}
