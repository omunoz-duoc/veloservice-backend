package com.veloservice.ordenes.infraestructure.persistence.repository;

import com.veloservice.ordenes.domain.model.TipoOrden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.List;


/**
 * Repository for the order type catalog.
 */
@Repository
public interface TipoOrdenRepository extends JpaRepository<TipoOrden, UUID> {
    /**
     * Encuentra un tipo de orden por su código.
     * @param codigo
     * @return
     */
    Optional<TipoOrden> findByCodigo(String codigo);

    /**
     * Busca todos los tipos de orden ordenados por código de forma ascendente.
     * @return
     */
    List<TipoOrden> findAllByOrderByCodigoAsc();
}
