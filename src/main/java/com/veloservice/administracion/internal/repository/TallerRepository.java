package com.veloservice.administracion.internal.repository;

import com.veloservice.administracion.internal.entity.Taller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for bike workshops.
 */
@Repository
public interface TallerRepository extends JpaRepository<Taller, UUID> {
    /**
     * Finds a workshop by its RUT.
     *
     * @param rut workshop RUT
     * @return matching workshop, if present
     */
    Optional<Taller> findByRut(String rut);
}