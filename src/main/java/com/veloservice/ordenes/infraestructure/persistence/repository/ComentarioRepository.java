package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.domain.model.OrdenComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for work order comments.
 */
@Repository
public interface ComentarioRepository extends JpaRepository<OrdenComentario, UUID> {
    /**
     * Lists comments for an order, ordered by creation time.
     *
     * @param ordenId work order identifier
     * @return comments ordered by creation date ascending
     */
    List<OrdenComentario> findByOrdenIdOrderByCreatedAtAsc(UUID ordenId);

    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.ComentarioResult(
            c.id, c.usuarioId, CONCAT(u.nombre, ' ', u.apellido), c.texto, c.createdAt
        )
        FROM OrdenComentario c
        JOIN com.veloservice.auth.domain.model.Usuario u ON u.id = c.usuarioId
        WHERE c.ordenId = :ordenId
        ORDER BY c.createdAt ASC
        """)
    List<ComentarioResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);

    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.ComentarioResult(
            c.id, c.usuarioId, CONCAT(u.nombre, ' ', u.apellido), c.texto, c.createdAt
        )
        FROM OrdenComentario c
        JOIN com.veloservice.auth.domain.model.Usuario u ON u.id = c.usuarioId
        WHERE c.id = :id
        """)
    Optional<ComentarioResult> findResultById(@Param("id") UUID id);
}
