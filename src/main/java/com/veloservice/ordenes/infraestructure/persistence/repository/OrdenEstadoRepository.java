package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.application.dto.OrdenEstadoResult;
import com.veloservice.ordenes.domain.model.OrdenEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdenEstadoRepository extends JpaRepository<OrdenEstado, UUID> {
    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.OrdenEstadoResult(
            oe.id,
            anterior.id,
            anterior.codigo,
            anterior.nombre,
            nuevo.id,
            nuevo.codigo,
            nuevo.nombre,
            oe.observacion,
            oe.usuarioId,
            COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sistema'),
            oe.createdAt
        )
        FROM OrdenEstado oe
        LEFT JOIN com.veloservice.ordenes.domain.model.EstadoOrden anterior ON anterior.id = oe.estadoAnteriorId
        JOIN com.veloservice.ordenes.domain.model.EstadoOrden nuevo ON nuevo.id = oe.estadoNuevoId
        LEFT JOIN com.veloservice.auth.domain.model.Usuario u ON u.id = oe.usuarioId
        WHERE oe.ordenId = :ordenId
        ORDER BY oe.createdAt ASC
        """)
    List<OrdenEstadoResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
}
