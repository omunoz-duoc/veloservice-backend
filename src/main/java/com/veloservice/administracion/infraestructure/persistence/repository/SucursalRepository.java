package com.veloservice.administracion.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.administracion.domain.model.Sucursal;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for workshop branches.
 */
@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, UUID> {
	/**
	 * Finds a branch by workshop identifier.
	 *
	 * @param tallerId workshop identifier
	 * @return matching branch, if present
	 */
	Optional<Sucursal> findFirstByTaller_Id(UUID tallerId);

	/**
	 * Finds a branch by its identifier, eagerly loading the associated Taller
	 * via JOIN FETCH to avoid LazyInitializationException outside a Hibernate session.
	 *
	 * @param id branch identifier
	 * @return matching branch with Taller loaded, if present
	 */
	@Query("SELECT s FROM Sucursal s JOIN FETCH s.taller WHERE s.id = :id")
	Optional<Sucursal> findByIdWithTaller(@Param("id") UUID id);
}