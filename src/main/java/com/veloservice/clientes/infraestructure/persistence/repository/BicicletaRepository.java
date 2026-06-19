package com.veloservice.clientes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.clientes.domain.model.Bicicleta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tenant bikes.
 */
@Repository
public interface BicicletaRepository extends JpaRepository<Bicicleta, UUID> {
    /**
     * Lists bikes for a customer.
     *
     * @param clienteId customer identifier
     * @return customer bikes
     */
    List<Bicicleta> findByClienteId(UUID clienteId);

    @Query("select b from Bicicleta b join b.cliente c where c.tallerId = :tallerId")
    List<Bicicleta> findAllByClienteTallerId(@Param("tallerId") UUID tallerId);

    @Query("select count(b) from Bicicleta b join b.cliente c where c.tallerId = :tallerId")
    long countByClienteTallerId(@Param("tallerId") UUID tallerId);

    @Query("select b from Bicicleta b join b.cliente c where b.id = :id and c.tallerId = :tallerId")
    Optional<Bicicleta> findByIdAndClienteTallerId(@Param("id") UUID id,
                                                   @Param("tallerId") UUID tallerId);
}
