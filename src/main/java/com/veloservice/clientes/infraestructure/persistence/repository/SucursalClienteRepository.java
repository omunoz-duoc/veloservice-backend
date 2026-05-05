package com.veloservice.clientes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.clientes.domain.model.SucursalCliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for branch-customer links.
 */
@Repository
public interface SucursalClienteRepository extends JpaRepository<SucursalCliente, UUID> {
    /**
     * Finds a branch-customer link.
     *
     * @param sucursalId branch identifier
     * @param clienteId customer identifier
     * @return link, if present
     */
    Optional<SucursalCliente> findBySucursalIdAndClienteId(UUID sucursalId, UUID clienteId);

    /**
     * Lists all branch-customer links for a branch.
     *
     * @param sucursalId branch identifier
     * @return branch customer links
     */
    List<SucursalCliente> findAllBySucursalId(UUID sucursalId);

    /**
     * Checks if a branch-customer link exists.
     *
     * @param sucursalId branch identifier
     * @param clienteId customer identifier
     * @return true if the link exists
     */
    boolean existsBySucursalIdAndClienteId(UUID sucursalId, UUID clienteId);
}