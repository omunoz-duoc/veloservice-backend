package com.veloservice.finanzas.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.finanzas.domain.model.Cobro;

import java.util.Optional;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.math.BigDecimal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for settlements.
 */
@Repository
public interface CobroRepository extends JpaRepository<Cobro, UUID> {
    /**
     * Finds a settlement by work order.
     *
     * @param ordenId work order identifier
     * @return matching settlement, if present
     */
    Optional<Cobro> findByOrdenId(UUID ordenId);

    @Query("select coalesce(sum(c.total), 0) from Cobro c where c.createdAt >= :start and c.createdAt < :end")
    BigDecimal sumTotalByCreatedAtBetween(@Param("start") OffsetDateTime start,
                                          @Param("end") OffsetDateTime end);

    @Query("select count(c) from Cobro c where c.createdAt >= :start and c.createdAt < :end")
    long countByCreatedAtBetween(@Param("start") OffsetDateTime start,
                                 @Param("end") OffsetDateTime end);

    @Query("select c.metodoPago, count(c) from Cobro c where c.createdAt >= :start and c.createdAt < :end group by c.metodoPago order by count(c) desc")
    java.util.List<Object[]> findMetodoPagoMasUsado(@Param("start") OffsetDateTime start,
                                                    @Param("end") OffsetDateTime end,
                                                    Pageable pageable);
}