package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.tenant.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByOrdenId(UUID ordenId);
}
