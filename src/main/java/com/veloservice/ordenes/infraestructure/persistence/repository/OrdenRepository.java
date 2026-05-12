package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.ordenes.application.dto.OrdenListaEntregaResult;
import com.veloservice.ordenes.application.dto.OrdenResumenResult;
import com.veloservice.ordenes.domain.model.Orden;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /**
     * Lists lightweight work order summaries for a tenant.
     *
     * @param sucursalId tenant identifier
     * @return lightweight work order summaries
     */
    @Query("""
                    select new com.veloservice.ordenes.application.dto.OrdenResumenResult(
                            o.externalId,
                            o.tipo,
                            o.fechaIngreso,
                            trim(concat(concat(coalesce(mec.nombre, ''), ' '), coalesce(mec.apellido, ''))),
                            trim(concat(concat(coalesce(cli.nombre, ''), ' '), coalesce(cli.apellido, ''))),
                            o.diagnosticoInicial,
                            o.observacionesCliente,
                            b.marca,
                            b.color,
                            b.tipo,
                            b.aro,
                            o.estado
                    )
                    from Orden o
                    join Bicicleta b on b.id = o.bicicletaId
                    join b.cliente cli
                    left join com.veloservice.administracion.domain.model.Usuario mec on mec.id = o.mecanicoId
                    where o.sucursalId = :sucursalId
                    order by o.fechaIngreso desc
                    """)
    List<OrdenResumenResult> findResumenBySucursalIdOrderByFechaIngresoDesc(@Param("sucursalId") UUID sucursalId);

        @Query("""
                        select new com.veloservice.ordenes.application.dto.OrdenListaEntregaResult(
                                o.id,
                                o.numeroOrden,
                                concat(coalesce(cli.nombre, ''), ' ', coalesce(cli.apellido, '')),
                                concat(coalesce(mec.nombre, ''), ' ', coalesce(mec.apellido, '')),
                                o.fechaIngreso,
                                o.estado,
                                coalesce((select sum(os.precioAplicado) from OrdenServicio os where os.ordenId = o.id), 0)
                                + coalesce((select sum(op.precioAplicado) from OrdenProducto op where op.ordenId = o.id), 0)
                        )
                        from Orden o
                        join Bicicleta b on b.id = o.bicicletaId
                        join b.cliente cli
                        left join com.veloservice.administracion.domain.model.Usuario mec on mec.id = o.mecanicoId
                        where o.sucursalId = :sucursalId
                            and o.estado = com.veloservice.config.enums.EstadoOrdenEnum.lista_para_entrega
                        order by o.fechaIngreso asc
                        """)
        List<OrdenListaEntregaResult> findListaEntregaBySucursalId(@Param("sucursalId") UUID sucursalId);
}