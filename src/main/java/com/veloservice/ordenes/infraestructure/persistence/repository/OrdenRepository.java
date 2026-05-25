package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.config.enums.EstadoOrdenEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    Optional<Orden> findByIdAndSucursalId(UUID id, UUID sucursalId);

    Optional<Orden> findByExternalIdAndSucursalId(String externalId, UUID sucursalId);

    List<Orden> findAllBySucursalIdOrderByFechaIngresoDesc(UUID sucursalId);

    List<Orden> findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(UUID sucursalId, UUID mecanicoId);

    List<Orden> findByMecanicoIdAndEstadoNotIn(UUID mecanicoId, List<EstadoOrdenEnum> estados);

    boolean existsByNumeroOrdenAndSucursalId(String numeroOrden, UUID sucursalId);

    @Query("SELECT o FROM Orden o JOIN Sucursal s ON o.sucursalId = s.id WHERE s.taller.id = :tallerId ORDER BY o.fechaIngreso DESC")
    List<Orden> findAllByTallerIdOrderByFechaIngresoDesc(@Param("tallerId") UUID tallerId);
}
