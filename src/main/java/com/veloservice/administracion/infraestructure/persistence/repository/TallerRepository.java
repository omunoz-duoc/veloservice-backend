package com.veloservice.administracion.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.administracion.domain.model.Taller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for bike workshops.
 */
@Repository
public interface TallerRepository extends JpaRepository<Taller, UUID> {

    /**
     * Encuentra un taller por su ID.
     * @param id
     * @return
     */
    Optional<Taller> findById(UUID id);

    /**
     * Encuentra un taller por su nombre.
     * @param nombre
     * @return
     */
    Optional<Taller> findByNombre(String nombre);

    /**
     * Lista todos los talleres activos.
     * @return
     */
    List<Taller> findAllByActivoTrue();

    /**
     * Lista todos los talleres asociados a un plan específico.
     * @param planId
     * @return
     */
    List<Taller> findAllByPlanId(UUID planId);

    /**
     * Encuentra un taller por su RUT.
     * @param rut
     * @return
     */
    Optional<Taller> findByRut(String rut);
}