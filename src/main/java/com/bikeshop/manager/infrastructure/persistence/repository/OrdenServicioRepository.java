package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.OrdenServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for work order services.
 */
@Repository
public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, UUID> {
    /**
     * Lists services for a work order.
     *
     * @param ordenId work order identifier
     * @return work order services
     */
    List<OrdenServicio> findByOrdenId(UUID ordenId);
}
