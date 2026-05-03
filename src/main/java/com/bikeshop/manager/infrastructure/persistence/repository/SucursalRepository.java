package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.platform.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
