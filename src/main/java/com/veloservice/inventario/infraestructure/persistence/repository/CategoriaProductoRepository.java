package com.veloservice.inventario.infraestructure.persistence.repository;

import com.veloservice.inventario.domain.model.CategoriaProducto;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, UUID> {
}
