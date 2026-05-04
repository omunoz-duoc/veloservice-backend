package com.veloservice.inventario.internal.repository;

import com.veloservice.inventario.internal.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for suppliers.
 */
@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, UUID> {
}