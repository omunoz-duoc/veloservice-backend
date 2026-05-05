package com.veloservice.clientes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.clientes.domain.model.Membresia;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for memberships.
 */
@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, UUID> {
}