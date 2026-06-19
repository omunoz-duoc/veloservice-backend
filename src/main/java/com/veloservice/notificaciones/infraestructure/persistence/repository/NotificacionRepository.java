package com.veloservice.notificaciones.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.notificaciones.domain.EstadoNotificacionEnum;
import com.veloservice.notificaciones.domain.model.Notificacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByOrdenId(UUID ordenId);

    @Query("select count(n) from Notificacion n join Orden o on o.id = n.ordenId where o.tallerId = :tallerId and n.estado = :estado")
    long countByTallerIdAndEstado(@Param("tallerId") UUID tallerId,
                                  @Param("estado") EstadoNotificacionEnum estado);
}
