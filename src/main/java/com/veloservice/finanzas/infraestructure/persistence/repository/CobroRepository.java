package com.veloservice.finanzas.infraestructure.persistence.repository;

import com.veloservice.finanzas.domain.model.Cobro;
import com.veloservice.finanzas.domain.EstadoCobroEnum;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CobroRepository extends JpaRepository<Cobro, UUID> {

    Optional<Cobro> findByOrdenId(UUID ordenId);

    @Query("select count(c) from Cobro c join Orden o on o.id = c.ordenId where o.tallerId = :tallerId")
    long countByTallerId(@Param("tallerId") UUID tallerId);

    @Query("select coalesce(sum(c.total), 0) from Cobro c where c.createdAt >= :start and c.createdAt < :end")
    BigDecimal sumTotalByCreatedAtBetween(@Param("start") OffsetDateTime start,
                                          @Param("end") OffsetDateTime end);

    @Query("select count(c) from Cobro c where c.createdAt >= :start and c.createdAt < :end")
    long countByCreatedAtBetween(@Param("start") OffsetDateTime start,
                                 @Param("end") OffsetDateTime end);

    @Query("select c.metodoPago, count(c) from Cobro c where c.createdAt >= :start and c.createdAt < :end group by c.metodoPago order by count(c) desc")
    List<Object[]> findMetodoPagoMasUsado(@Param("start") OffsetDateTime start,
                                          @Param("end") OffsetDateTime end,
                                          Pageable pageable);

    @Query("""
        select coalesce(sum(c.total), 0),
               coalesce(sum(c.subtotalProductos), 0),
               coalesce(sum(c.subtotalServicios), 0),
               count(c)
        from Cobro c
        join Orden o on o.id = c.ordenId
        where coalesce(c.fechaPago, c.createdAt) >= :start
          and coalesce(c.fechaPago, c.createdAt) < :end
          and c.estado = :estado
          and c.anuladaAt is null
          and o.sucursalId = :sucursalId
    """)
    Object[] sumRentabilidadByCreatedAtBetween(@Param("start") OffsetDateTime start,
                                               @Param("end") OffsetDateTime end,
                                               @Param("estado") EstadoCobroEnum estado,
                                               @Param("sucursalId") UUID sucursalId);
}
