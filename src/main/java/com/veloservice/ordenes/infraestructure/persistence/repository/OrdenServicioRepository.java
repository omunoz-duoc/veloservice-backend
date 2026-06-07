package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.domain.model.OrdenServicio;

import java.util.List;
import java.util.Optional;
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

    Optional<OrdenServicio> findByIdAndOrdenId(UUID id, UUID ordenId);

    Optional<OrdenServicio> findByOrdenIdAndServicioId(UUID ordenId, UUID servicioId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
        DELETE FROM OrdenServicio os
        WHERE os.id = :id
          AND os.ordenId = :ordenId
        """)
    int deleteByIdAndOrdenId(@Param("id") UUID id, @Param("ordenId") UUID ordenId);

    /**
     * Encuentra todos los servicios asociados a una orden
     */
    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.OrdenServicioResult(
            os.id,
            os.servicioId,
            s.nombre,
            os.precioBaseSnapshot,
            os.precioAplicado,
            os.descuentoAplicado,
            os.notas
        )
        FROM OrdenServicio os
        JOIN com.veloservice.servicios.domain.model.Servicio s ON s.id = os.servicioId
        WHERE os.ordenId = :ordenId
        ORDER BY os.createdAt ASC
        """)
    List<OrdenServicioResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);

    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.OrdenServicioResult(
            os.id,
            os.servicioId,
            s.nombre,
            os.precioBaseSnapshot,
            os.precioAplicado,
            os.descuentoAplicado,
            os.notas
        )
        FROM OrdenServicio os
        JOIN com.veloservice.servicios.domain.model.Servicio s ON s.id = os.servicioId
        WHERE os.id IN :ids
        """)
    List<OrdenServicioResult> findResultByIdIn(@Param("ids") List<UUID> ids);
}
