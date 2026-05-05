package com.veloservice.notificaciones.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.notificaciones.domain.model.Notificacion;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByOrdenId(UUID ordenId);
}