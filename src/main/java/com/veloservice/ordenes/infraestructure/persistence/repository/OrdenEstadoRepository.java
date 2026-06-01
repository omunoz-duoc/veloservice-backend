package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.domain.model.OrdenEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrdenEstadoRepository extends JpaRepository<OrdenEstado, UUID> {
}
