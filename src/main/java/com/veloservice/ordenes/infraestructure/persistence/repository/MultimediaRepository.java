package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.domain.EtapaMultimediaEnum;
import com.veloservice.ordenes.domain.model.Multimedia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
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

    Optional<Multimedia> findByObjectKey(String objectKey);

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
            m.id,
            m.usuarioId,
            CONCAT(u.nombre, ' ', u.apellido),
            m.tipoArchivo,
            CASE
                WHEN m.tipoArchivo LIKE 'image/%' OR m.tipoArchivo = 'imagen' THEN 'imagen'
                WHEN m.tipoArchivo LIKE 'video/%' OR m.tipoArchivo = 'video' THEN 'video'
                ELSE 'documento'
            END,
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

    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.MultimediaResult(
            m.id, m.usuarioId, CONCAT(u.nombre, ' ', u.apellido),
            m.tipoArchivo,
            CASE
                WHEN m.tipoArchivo LIKE 'image/%' OR m.tipoArchivo = 'imagen' THEN 'imagen'
                WHEN m.tipoArchivo LIKE 'video/%' OR m.tipoArchivo = 'video' THEN 'video'
                ELSE 'documento'
            END,
            m.url, CAST(m.etapa AS string), m.descripcion, m.createdAt
        )
        FROM Multimedia m
        JOIN com.veloservice.auth.domain.model.Usuario u ON u.id = m.usuarioId
        WHERE m.id = :id
        """)
    Optional<MultimediaResult> findResultById(@Param("id") UUID id);
}
