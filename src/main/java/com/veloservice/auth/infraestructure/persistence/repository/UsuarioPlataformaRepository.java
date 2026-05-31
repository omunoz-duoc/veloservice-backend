package com.veloservice.auth.infraestructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.auth.domain.model.UsuarioPlataforma;

@Repository
public interface UsuarioPlataformaRepository extends JpaRepository<UsuarioPlataforma, UUID> {
    Optional<UsuarioPlataforma> findByEmailAndActivoTrue(String email);
}
