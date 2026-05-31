package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.domain.model.EstadoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for the order status catalog.
 */
@Repository
public interface EstadoOrdenCatalogRepository extends JpaRepository<EstadoOrden, UUID> {
    Optional<EstadoOrden> findByCodigo(String codigo);

    List<EstadoOrden> findByCodigoIn(Collection<String> codigos);
}
