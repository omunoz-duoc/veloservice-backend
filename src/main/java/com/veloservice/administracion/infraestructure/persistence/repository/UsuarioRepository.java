package com.veloservice.administracion.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.administracion.domain.model.Usuario;

import java.util.List;
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

        @Query("""
                        select u
                        from Usuario u
                        join fetch u.rol r
                        join fetch u.sucursal s
                        where upper(r.nombre) = 'MECANICO'
                            and s.id = :sucursalId
                            and (:activo is null or u.activo = :activo)
                        order by u.apellido asc, u.nombre asc
                        """)
        List<Usuario> findMecanicosBySucursalIdAndActivo(@Param("sucursalId") UUID sucursalId,
                                                                                                         @Param("activo") Boolean activo);
}