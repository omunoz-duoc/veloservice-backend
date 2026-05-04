package com.veloservice.crm.internal.repository;

import com.veloservice.crm.internal.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for customers.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    /**
     * Finds a customer by RUT.
     *
     * @param rut customer RUT
     * @return matching customer, if present
     */
    Optional<Cliente> findByRut(String rut);

    /**
     * Checks if a RUT already exists.
     *
     * @param rut customer RUT
     * @return true if a customer already uses the RUT
     */
    boolean existsByRut(String rut);
}