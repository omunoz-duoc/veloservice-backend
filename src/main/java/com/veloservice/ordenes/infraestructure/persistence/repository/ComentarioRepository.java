package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.domain.model.OrdenComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
}
