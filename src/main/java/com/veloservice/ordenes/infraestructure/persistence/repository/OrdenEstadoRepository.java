package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.application.dto.OrdenActividadRecienteResult;
import com.veloservice.ordenes.domain.model.OrdenEstado;

import java.util.List;
import java.util.UUID;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Pageable;

/**
 * Repository for order state audit entries.
 */
@Repository
public interface OrdenEstadoRepository extends JpaRepository<OrdenEstado, UUID> {
    /**
     * Lists audit entries for an order.
     *
     * @param ordenId work order identifier
     * @return ordered audit entries
     */
    List<OrdenEstado> findByOrdenIdOrderByCreatedAtAsc(UUID ordenId);

            @Query("""
            select new com.veloservice.ordenes.application.dto.OrdenActividadRecienteResult(
                o.id,
                case when e.estadoAnteriorId is null then 'CREADA' else 'ESTADO_CAMBIADO' end,
                e.createdAt,
                case when e.estadoAnteriorId is null
                    then concat('Orden creada: ', o.numeroOrden)
                    else concat('Estado cambiado en orden ', o.numeroOrden)
                end,
                concat(coalesce(u.nombre, ''), ' ', coalesce(u.apellido, ''))
            )
            from OrdenEstado e
            join com.veloservice.ordenes.domain.model.Orden o on o.id = e.ordenId
            join com.veloservice.clientes.domain.model.Bicicleta b on b.id = o.bicicletaId
            join b.cliente cli
            left join com.veloservice.auth.domain.model.Usuario u on u.id = e.usuarioId
            where o.sucursalId = :sucursalId
              and e.createdAt >= :since
            order by e.createdAt desc
            """)
    List<OrdenActividadRecienteResult> findActividadRecienteBySucursalIdSince(@Param("sucursalId") UUID sucursalId,
                                                                              @Param("since") OffsetDateTime since,
                                                                              Pageable pageable);
}
