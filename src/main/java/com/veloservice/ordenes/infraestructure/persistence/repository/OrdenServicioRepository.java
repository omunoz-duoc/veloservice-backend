package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.domain.model.OrdenServicio;

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

    /**
     * Encuentra todos los servicios asociados a una orden
     */
    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.OrdenServicioResult(
            os.id,
            s.id,
            s.nombre,
            s.precioBase
        )
        FROM OrdenServicio os
        JOIN com.veloservice.servicios.domain.model.Servicio s ON s.id = os.servicioId
        WHERE os.ordenId = :ordenId
        ORDER BY os.createdAt ASC
        """)
    List<OrdenServicioResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
}
