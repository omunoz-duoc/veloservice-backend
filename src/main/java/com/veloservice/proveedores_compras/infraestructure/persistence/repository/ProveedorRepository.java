package com.veloservice.proveedores_compras.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.proveedores_compras.domain.model.Proveedor;

import java.util.UUID;

/**
 * Repository for suppliers.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, UUID> {
}