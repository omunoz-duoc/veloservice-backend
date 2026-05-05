package com.veloservice.servicios.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.servicios.domain.model.Servicio;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for base services.
 */
@Repository
public interface ServicioRepository extends JpaRepository<Servicio, UUID> {
    /**
     * Finds a service by identifier.
     *
     * @param id service identifier
     * @return matching service, if present
     */
    Optional<Servicio> findById(UUID id);
}