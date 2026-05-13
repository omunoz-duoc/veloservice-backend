package com.veloservice.clientes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.clientes.domain.model.Cliente;

import java.util.List;
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
     * Finds a customer by email.
     *
     * @param email customer email
     * @return matching customer, if present
     */
    Optional<Cliente> findByEmailIgnoreCase(String email);

    /**
     * Checks if a RUT already exists.
     *
     * @param rut customer RUT
     * @return true if a customer already uses the RUT
     */
    boolean existsByRut(String rut);

        /**
         * Searches customers within the given branch.
         *
         * @param sucursalId branch identifier
         * @param texto search text
         * @return matching customers
         */
        @Query(value = """
                        SELECT c.*
                        FROM clientes c
                        JOIN sucursal_clientes sc ON sc.cliente_id = c.id
                        WHERE sc.sucursal_id = :sucursalId
                            AND (
                                c.nombre ILIKE CONCAT('%', :texto, '%')
                                OR c.apellido ILIKE CONCAT('%', :texto, '%')
                                OR (c.nombre || ' ' || c.apellido) ILIKE CONCAT('%', :texto, '%')
                                OR c.email ILIKE CONCAT('%', :texto, '%')
                                OR c.telefono ILIKE CONCAT('%', :texto, '%')
                                OR c.rut ILIKE CONCAT('%', :texto, '%')
                            )
                        ORDER BY c.nombre
                        LIMIT 10
                        """, nativeQuery = true)
        List<Cliente> buscarPorSucursal(@Param("sucursalId") UUID sucursalId, @Param("texto") String texto);
}