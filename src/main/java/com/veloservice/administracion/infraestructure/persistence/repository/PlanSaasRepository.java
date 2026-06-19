package com.veloservice.administracion.infraestructure.persistence.repository;

import com.veloservice.administracion.domain.model.PlanSaas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanSaasRepository extends JpaRepository<PlanSaas, UUID> {
    boolean existsByCodigoIgnoreCase(String codigo);

    List<PlanSaas> findAllByActivoTrueOrderByOrdenAsc();
}
