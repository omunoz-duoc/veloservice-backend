package com.veloservice.administracion.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.veloservice.administracion.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for tenant users.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    /**
     * Finds an active user by email.
     *
     * @param email user email
     * @return matching user, if present
     */
    Optional<Usuario> findByEmailAndActivoTrue(String email);

    /**
     * Finds a user by email.
     *
     * @param email user email
     * @return matching user, if present
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Checks if a user exists with the given email.
     *
     * @param email user email
     * @return true if the email is already registered
     */
    boolean existsByEmail(String email);
}