package com.veloservice.ordenes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.config.enums.EstadoOrdenEnum;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, UUID> {

    Optional<Orden> findByIdAndSucursalId(UUID id, UUID sucursalId);

    List<Orden> findAllBySucursalIdOrderByFechaIngresoDesc(UUID sucursalId);

    List<Orden> findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(UUID sucursalId, UUID mecanicoId);

    List<Orden> findByMecanicoIdAndEstadoNotIn(UUID mecanicoId, List<EstadoOrdenEnum> estados);

    boolean existsByNumeroOrdenAndSucursalId(String numeroOrden, UUID sucursalId);
}
