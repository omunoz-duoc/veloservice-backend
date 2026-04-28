package com.bikeshop.manager.infrastructure.persistence.repository;

import com.bikeshop.manager.domain.platform.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for platform and tenant roles.
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, UUID> {
    /**
     * Finds a role by name.
     *
     * @param nombre role name
     * @return matching role, if present
     */
    Optional<Rol> findByNombre(String nombre);
}
