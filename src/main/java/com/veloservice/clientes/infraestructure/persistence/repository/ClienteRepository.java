package com.veloservice.clientes.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.clientes.application.dto.ClienteResumenResult;
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
     * Finds all customers for a sucursal with aggregated data including bike count,
     * order count, and total spending.
     * Returns raw data that can be mapped to ClienteResumenResult.
     * 
     * @param sucursalId the sucursal identifier to filter orders
     * @return customers with summary data as Object arrays:
     *         [externalId, nombreCompleto, rut, telefono, email, numeroBicicletas, numeroOrdenes, gastoTotal]
     */
    @Query(value = """
        SELECT 
            c.external_id,
            TRIM(CONCAT(COALESCE(c.nombre, ''), ' ', COALESCE(c.apellido, ''))),
            c.rut,
            c.telefono,
            c.email,
            COALESCE(COUNT(DISTINCT b.id), 0),
            COALESCE(COUNT(DISTINCT o.id), 0),
            COALESCE(SUM(COALESCE(os.precio_aplicado, 0) + COALESCE(op.precio_aplicado, 0)), 0)
        FROM clientes c
        LEFT JOIN bicicletas b ON b.cliente_id = c.id
        LEFT JOIN ordenes o ON o.bicicleta_id = b.id AND o.sucursal_id = :sucursalId
        LEFT JOIN orden_servicios os ON os.orden_id = o.id
        LEFT JOIN orden_productos op ON op.orden_id = o.id
        GROUP BY c.id, c.external_id, c.nombre, c.apellido, c.rut, c.telefono, c.email
        ORDER BY c.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findResumenRawBySucursalId(@Param("sucursalId") UUID sucursalId);

    /**
     * Finds a customer by RUT.
     *
     * @param rut customer RUT
     * @return matching customer, if present
     */
    Optional<Cliente> findByRut(String rut);

    Optional<Cliente> findByTallerIdAndRut(UUID tallerId, String rut);

    List<Cliente> findAllByTallerIdOrderByCreatedAtDesc(UUID tallerId);

    Optional<Cliente> findByIdAndTallerId(UUID id, UUID tallerId);

    boolean existsByIdAndTallerId(UUID id, UUID tallerId);

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
                        WHERE c.taller_id = :tallerId
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
        List<Cliente> buscarPorTaller(@Param("tallerId") UUID tallerId, @Param("texto") String texto);
}
