package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.domain.EtapaMultimediaEnum;
import com.veloservice.ordenes.domain.model.Multimedia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for order multimedia evidence.
 */
@Repository
public interface MultimediaRepository extends JpaRepository<Multimedia, UUID> {
    /**
     * Lists multimedia entries for an order.
     *
     * @param ordenId work order identifier
     * @return multimedia entries
     */
    List<Multimedia> findByOrdenId(UUID ordenId);

    /**
     * Checks if an order has evidence for a stage.
     *
     * @param ordenId work order identifier
     * @param etapa stage name
     * @return true when evidence exists
     */
    boolean existsByOrdenIdAndEtapa(UUID ordenId, EtapaMultimediaEnum etapa);

    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.MultimediaResult(
            CONCAT(u.nombre, ' ', u.apellido),
            CAST(m.tipoArchivo AS string),
            m.url,
            CAST(m.etapa AS string),
            m.descripcion,
            m.createdAt
        )
        FROM Multimedia m
        JOIN com.veloservice.auth.domain.model.Usuario u ON u.id = m.usuarioId
        WHERE m.ordenId = :ordenId
        ORDER BY m.createdAt ASC
        """)
    List<MultimediaResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
}