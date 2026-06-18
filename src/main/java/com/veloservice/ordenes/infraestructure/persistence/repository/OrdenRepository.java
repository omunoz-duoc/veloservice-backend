package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.veloservice.ordenes.application.dto.OrdenDetalleBaseResult;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.application.dto.OrdenResumenClienteResult;
import com.veloservice.ordenes.domain.model.Orden;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    String READ_SELECT = """
            select new com.veloservice.ordenes.application.dto.OrdenReadResult(
                o.id,
                o.numeroOrden,
                o.tallerId,
                o.sucursalId,
                e.id,
                e.codigo,
                e.nombre,
                t.id,
                t.codigo,
                t.nombre,
                o.fechaIngreso,
                o.fechaPrometida,
                o.fechaEntrega,
                o.diagnosticoInicial,
                o.diagnosticoFinal,
                o.observacionesCliente,
                b.id,
                b.marca,
                b.modelo,
                b.tipo,
                b.aro,
                b.color,
                b.numeroSerie,
                b.anio,
                b.notas,
                c.id,
                c.nombre,
                c.apellido,
                c.telefono,
                c.email,
                c.rut,
                c.direccion,
                u.id,
                u.nombre,
                u.apellido,
                o.prioridad
            )
            from Orden o
            join com.veloservice.ordenes.domain.model.EstadoOrden e on e.id = o.estadoId
            join com.veloservice.ordenes.domain.model.TipoOrden t on t.id = o.tipoId
            join com.veloservice.clientes.domain.model.Bicicleta b on b.id = o.bicicletaId
            join com.veloservice.clientes.domain.model.Cliente c on c.id = b.clienteId
            left join com.veloservice.auth.domain.model.Usuario u on u.id = o.mecanicoId
            """;

    String DETALLE_SELECT = """
            select new com.veloservice.ordenes.application.dto.OrdenDetalleBaseResult(
                o.id,
                o.numeroOrden,
                o.tallerId,
                o.sucursalId,
                e.id,
                e.codigo,
                e.nombre,
                t.id,
                t.codigo,
                t.nombre,
                o.fechaIngreso,
                o.fechaPrometida,
                o.fechaEntrega,
                o.diagnosticoInicial,
                o.diagnosticoFinal,
                o.observacionesCliente,
                b.id,
                b.marca,
                b.modelo,
                b.tipo,
                b.aro,
                b.color,
                b.numeroSerie,
                b.anio,
                b.fotoUrl,
                b.notas,
                c.id,
                c.nombre,
                c.apellido,
                c.telefono,
                c.email,
                c.rut,
                c.direccion,
                u.id,
                u.nombre,
                u.apellido,
                o.prioridad
            )
            from Orden o
            join com.veloservice.ordenes.domain.model.EstadoOrden e on e.id = o.estadoId
            join com.veloservice.ordenes.domain.model.TipoOrden t on t.id = o.tipoId
            join com.veloservice.clientes.domain.model.Bicicleta b on b.id = o.bicicletaId
            join com.veloservice.clientes.domain.model.Cliente c on c.id = b.clienteId
            left join com.veloservice.auth.domain.model.Usuario u on u.id = o.mecanicoId
            """;

    @Query(DETALLE_SELECT + """
            where o.id = :id
              and o.tallerId = :tallerId
            """)
    Optional<OrdenDetalleBaseResult> findDetalleBaseByIdAndTallerId(@Param("id") UUID id,
                                                                    @Param("tallerId") UUID tallerId);

    @Query(DETALLE_SELECT + """
            where o.id = :id
              and o.sucursalId = :sucursalId
            """)
    Optional<OrdenDetalleBaseResult> findDetalleBaseByIdAndSucursalId(@Param("id") UUID id,
                                                                      @Param("sucursalId") UUID sucursalId);

    @Query(DETALLE_SELECT + """
            where o.numeroOrden = :numeroOrden
              and o.tallerId = :tallerId
            """)
    Optional<OrdenDetalleBaseResult> findDetalleBaseByNumeroOrdenAndTallerId(@Param("numeroOrden") String numeroOrden,
                                                                             @Param("tallerId") UUID tallerId);

    @Query(DETALLE_SELECT + """
            where o.numeroOrden = :numeroOrden
              and o.sucursalId = :sucursalId
            """)
    Optional<OrdenDetalleBaseResult> findDetalleBaseByNumeroOrdenAndSucursalId(@Param("numeroOrden") String numeroOrden,
                                                                               @Param("sucursalId") UUID sucursalId);

    @Query(READ_SELECT + """
            where o.tallerId = :tallerId
            order by o.fechaIngreso desc
            """)
    List<OrdenReadResult> findReadByTallerId(@Param("tallerId") UUID tallerId);

    @Query(READ_SELECT + """
            where o.sucursalId = :sucursalId
            order by o.fechaIngreso desc
            """)
    List<OrdenReadResult> findReadBySucursalId(@Param("sucursalId") UUID sucursalId);

    @Query(READ_SELECT + """
            where o.sucursalId = :sucursalId
              and o.mecanicoId = :mecanicoId
            order by o.fechaIngreso desc
            """)
    List<OrdenReadResult> findReadBySucursalIdAndMecanicoId(@Param("sucursalId") UUID sucursalId,
                                                            @Param("mecanicoId") UUID mecanicoId);

    @Query(READ_SELECT + """
            where o.id = :id
              and o.tallerId = :tallerId
            """)
    Optional<OrdenReadResult> findReadByIdAndTallerId(@Param("id") UUID id,
                                                      @Param("tallerId") UUID tallerId);

    @Query(READ_SELECT + """
            where o.id = :id
              and o.sucursalId = :sucursalId
            """)
    Optional<OrdenReadResult> findReadByIdAndSucursalId(@Param("id") UUID id,
                                                        @Param("sucursalId") UUID sucursalId);

    @Query(READ_SELECT + """
            where o.numeroOrden = :numeroOrden
              and o.tallerId = :tallerId
            """)
    Optional<OrdenReadResult> findReadByNumeroOrdenAndTallerId(@Param("numeroOrden") String numeroOrden,
                                                               @Param("tallerId") UUID tallerId);

    @Query(READ_SELECT + """
            where o.numeroOrden = :numeroOrden
              and o.sucursalId = :sucursalId
            """)
    Optional<OrdenReadResult> findReadByNumeroOrdenAndSucursalId(@Param("numeroOrden") String numeroOrden,
                                                                 @Param("sucursalId") UUID sucursalId);

    /**
     * Encuentra todas las órdenes asociadas a un taller específico, ordenadas por fecha de ingreso descendente.
     * Este método es útil para obtener la lista de órdenes por taller, cuando el usuario tenga el rol de admin_taller
     * o jefe_taller, ya que ambos roles pueden gestionar órdenes a nivel de taller.
     * @param tallerId
     * @return
     */
    List<Orden> findAllByTallerIdOrderByFechaIngresoDesc(UUID tallerId);

    /**
     * Encuentra todas las órdenes asociadas a una sucursal específica, ordenadas por fecha de ingreso descendente.
     * @param sucursalId
     * @return
     */
    List<Orden> findAllBySucursalIdOrderByFechaIngresoDesc(UUID sucursalId);

    /**
     * Encuentra todas las órdenes de un taller dentro de un rango de fechas de ingreso específico, ordenadas por fecha de ingreso descendente.
     * @param tallerId
     * @param from
     * @param to
     * @return
     */
    List<Orden> findAllByTallerIdAndFechaIngresoBetweenOrderByFechaIngresoDesc(UUID tallerId, OffsetDateTime from, OffsetDateTime to);


    /**
     * Encuentra todas las órdenes de una sucursal dentro de un rango de fechas de ingreso específico, ordenadas por fecha de ingreso descendente.
     * @param sucursalId
     * @param from
     * @param to
     * @return
     */
    List<Orden> findAllBySucursalIdAndFechaIngresoBetweenOrderByFechaIngresoDesc(UUID sucursalId, OffsetDateTime from, OffsetDateTime to);


    /**
     * Encuentra todas las órdenes asociadas a una bicicleta específica, ordenadas por fecha de ingreso descendente.
     * @param bicicletaId
     * @return
     */
    List<Orden> findAllByBicicletaIdOrderByFechaIngresoDesc(UUID bicicletaId);

    boolean existsByBicicletaId(UUID bicicletaId);

    /**
     * Encuentra todas las órdenes asociadas a un mecánico específico, ordenadas por fecha de ingreso descendente.
     * @param mecanicoId
     * @return
     */
    List<Orden> findAllByMecanicoIdOrderByFechaIngresoDesc(UUID mecanicoId);

    /**
     * Encuentra todas las órdenes asociadas a un mecánico específico y con un estado específico, ordenadas por fecha de ingreso descendente.
     * @param mecanicoId
     * @param estadoId
     * @return
     */
    List<Orden> findAllByMecanicoIdAndEstadoIdOrderByFechaIngresoDesc(UUID mecanicoId, UUID estadoId);

    /**
     * Encuentra una orden por su ID y la ID del taller, asegurando que la orden pertenece al taller especificado.
     * @param id
     * @param tallerId
     * @return
     */
    Optional<Orden> findByIdAndTallerId(UUID id, UUID tallerId);

    /**
     * Encuentra una orden por su ID y la ID de la sucursal, asegurando que la orden pertenece a la sucursal especificada.
     * @param id
     * @param sucursalId
     * @return
     */
    Optional<Orden> findByIdAndSucursalId(UUID id, UUID sucursalId);

    /**
     * Cuenta el número de órdenes asociadas a un taller y a un estado en específico.
     * @param tallerId
     * @param estadoId
     * @return
     */
    long countByTallerIdAndEstadoId(UUID tallerId, UUID estadoId);

    long countByTallerId(UUID tallerId);

    long countByTallerIdAndFechaIngresoGreaterThanEqual(UUID tallerId, OffsetDateTime desde);

    /**
     * Cuenta el número de órdenes asociadas a una sucursal y a un estado en específico.
     * @param sucursalId
     * @param estadoId
     * @return
     */
    long countBySucursalIdAndEstadoId(UUID sucursalId, UUID estadoId);

    @Query("""
            select count(o)
            from Orden o
            join com.veloservice.ordenes.domain.model.EstadoOrden e on e.id = o.estadoId
            where o.mecanicoId = :mecanicoId
              and o.sucursalId = :sucursalId
              and e.codigo in :codigos
            """)
    long countActivasByMecanicoIdAndSucursalId(@Param("mecanicoId") UUID mecanicoId,
                                               @Param("sucursalId") UUID sucursalId,
                                               @Param("codigos") List<String> codigos);

    @Query("""
            select count(o)
            from Orden o
            join com.veloservice.ordenes.domain.model.EstadoOrden e on e.id = o.estadoId
            where o.mecanicoId = :mecanicoId
              and o.tallerId = :tallerId
              and e.codigo in :codigos
            """)
    long countActivasByMecanicoIdAndTallerId(@Param("mecanicoId") UUID mecanicoId,
                                             @Param("tallerId") UUID tallerId,
                                             @Param("codigos") List<String> codigos);


    /**
     * Encuentra todas las órdenes asociadas a una sucursal y a un mecánico específico, ordenadas por fecha de ingreso descendente.
     * @param sucursalId
     * @param mecanicoId
     * @return
     */
    List<Orden> findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(UUID sucursalId, UUID mecanicoId);

    /**
     * Encuentra una orden por su número de orden y la ID de la sucursal, asegurando que la orden pertenece a la sucursal especificada.
     * @param numeroOrden
     * @param sucursalId
     * @return
     */
    Optional<Orden> findByNumeroOrdenAndSucursalId(String numeroOrden, UUID sucursalId);

    /**
     * Encuentra una orden por su número de orden y la ID del taller, asegurando que la orden pertenece al taller especificado.
     * @param numeroOrden
     * @param tallerId
     * @return
     */
    Optional<Orden> findByNumeroOrdenAndTallerId(String numeroOrden, UUID tallerId);

    /**
     * Verifica si existe una orden con un número de orden específico dentro de un taller, lo que es útil para validar la 
     * unicidad del número de orden al crear o actualizar una orden.
     * @param numeroOrden
     * @param tallerId
     * @return
     */
    boolean existsByNumeroOrdenAndTallerId(String numeroOrden, UUID tallerId);

    Optional<Orden> findFirstByTallerIdOrderByCreatedAtDesc(UUID tallerId);

    @Query("select o.numeroOrden from Orden o where o.tallerId = :tallerId")
    List<String> findNumerosOrdenByTallerId(@Param("tallerId") UUID tallerId);

    @Query("""
            select count(o)
            from Orden o
            join com.veloservice.clientes.domain.model.Bicicleta b on b.id = o.bicicletaId
            where b.clienteId = :clienteId
              and o.tallerId = :tallerId
            """)
    long countByClienteIdAndTallerId(@Param("clienteId") UUID clienteId,
                                     @Param("tallerId") UUID tallerId);

    @Query("""
            select new com.veloservice.ordenes.application.dto.OrdenResumenClienteResult(
                o.numeroOrden, t.nombre, e.nombre, o.fechaIngreso)
            from Orden o
            join com.veloservice.ordenes.domain.model.TipoOrden t on t.id = o.tipoId
            join com.veloservice.ordenes.domain.model.EstadoOrden e on e.id = o.estadoId
            join com.veloservice.clientes.domain.model.Bicicleta b on b.id = o.bicicletaId
            where b.clienteId = :clienteId
              and o.tallerId = :tallerId
            order by o.fechaIngreso desc
            """)
    List<OrdenResumenClienteResult> findResumenByClienteIdAndTallerId(
            @Param("clienteId") UUID clienteId,
            @Param("tallerId") UUID tallerId,
            Pageable pageable);
}
