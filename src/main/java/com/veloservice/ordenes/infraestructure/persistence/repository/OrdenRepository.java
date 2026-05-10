package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.application.dto.OrdenUrgenteResult;
import com.veloservice.ordenes.domain.model.Orden;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Repository for work orders.
 */
@Repository
public interface OrdenRepository extends JpaRepository<Orden, UUID> {
    /**
     * Finds a work order by identifier and tenant.
     *
     * @param id work order identifier
     * @param tallerId tenant identifier
     * @return matching work order, if present
     */
    Optional<Orden> findByIdAndSucursalId(UUID id, UUID sucursalId);

    /**
     * Lists work orders for a tenant in descending creation order.
     *
     * @param tallerId tenant identifier
     * @return tenant work orders
     */
    List<Orden> findAllBySucursalIdOrderByFechaIngresoDesc(UUID sucursalId);

    /**
     * Checks if a sequence is already used for a tenant.
     *
     * @param numeroOrden sequence number
     * @param tallerId tenant identifier
     * @return true if the sequence exists
     */
    boolean existsByNumeroOrdenAndSucursalId(String numeroOrden, UUID sucursalId);

        @Query("""
                        select new com.veloservice.ordenes.application.dto.OrdenUrgenteResult(
                                o.id,
                                o.numeroOrden,
                                concat(coalesce(cli.nombre, ''), ' ', coalesce(cli.apellido, '')),
                                o.estado,
                                o.fechaIngreso
                        )
                        from Orden o
                        join com.veloservice.clientes.domain.model.Bicicleta b on b.id = o.bicicletaId
                        join b.cliente cli
                        where o.sucursalId = :sucursalId
                            and o.estado = com.veloservice.config.enums.EstadoOrdenEnum.recibida
                            and o.createdAt < :cutoff
                            and not exists (
                                    select 1
                                    from OrdenEstado e
                                    where e.ordenId = o.id
                                        and e.estadoNuevo <> com.veloservice.config.enums.EstadoOrdenEnum.recibida
                            )
                        order by o.fechaIngreso asc
                        """)
        List<OrdenUrgenteResult> findUrgentesBySucursalId(@Param("sucursalId") UUID sucursalId,
                                                                                                            @Param("cutoff") java.time.OffsetDateTime cutoff,
                                                                                                            Pageable pageable);
}