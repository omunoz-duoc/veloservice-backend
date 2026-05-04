package com.veloservice.crm.internal.repository;

import com.veloservice.crm.internal.entity.Membresia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for memberships.
 */
@Repository
public interface MembresiaRepository extends JpaRepository<Membresia, UUID> {
}