package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.domain.EtapaMultimediaEnum;
import com.veloservice.ordenes.domain.model.Multimedia;

import org.springframework.data.jpa.repository.JpaRepository;
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
}