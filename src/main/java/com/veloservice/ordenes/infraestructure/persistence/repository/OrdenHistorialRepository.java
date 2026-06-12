package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.application.dto.OrdenHistorialResult;
import com.veloservice.ordenes.domain.model.OrdenHistorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrdenHistorialRepository extends JpaRepository<OrdenHistorial, UUID> {

    @Query("""
        SELECT new com.veloservice.ordenes.application.dto.OrdenHistorialResult(
            h.id,
            h.ordenId,
            h.accion,
            h.entidad,
            h.entidadId,
            h.detalle,
            h.usuarioId,
            COALESCE(CONCAT(u.nombre, ' ', u.apellido), 'Sistema'),
            h.createdAt
        )
        FROM OrdenHistorial h
        LEFT JOIN com.veloservice.auth.domain.model.Usuario u ON u.id = h.usuarioId
        WHERE h.ordenId = :ordenId
        ORDER BY h.createdAt DESC
        """)
    // Orden descendente: eventos más recientes primero, para mostrar el timeline en la UI.
    List<OrdenHistorialResult> findResultByOrdenId(@Param("ordenId") UUID ordenId);
}
