package com.veloservice.administracion.infraestructure.persistence.repository;

import com.veloservice.administracion.domain.model.UsuarioSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioSucursalRepository extends JpaRepository<UsuarioSucursal, UUID> {
    Optional<UsuarioSucursal> findByUsuarioIdAndEsPrincipalTrue(UUID usuarioId);
}
