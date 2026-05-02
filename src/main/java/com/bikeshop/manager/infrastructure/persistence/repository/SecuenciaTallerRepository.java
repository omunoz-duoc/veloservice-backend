package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.platform.SecuenciaTaller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tenant sequences.
 */
@Repository
public interface SecuenciaTallerRepository extends JpaRepository<SecuenciaTaller, UUID> {

    /**
     * Locks and retrieves a sequence by tenant, type, and year.
     *
     * @param tallerId tenant identifier
     * @param tipo sequence type
     * @param anio year
     * @return sequence, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SecuenciaTaller> findByTallerIdAndTipoAndAnio(UUID tallerId, String tipo, Integer anio);
}
