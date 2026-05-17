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

    /**
     * Lists users by role for a specific branch.
     *
     * @param sucursalId branch identifier
     * @param rolNombre role name
     * @return users with the specified role
     */
    List<Usuario> findBySucursalIdAndRolNombre(UUID sucursalId, String rolNombre);

    /**
     * Lists users by role and active status for a specific branch.
     *
     * @param sucursalId branch identifier
     * @param rolNombre role name
     * @param activo active status filter
     * @return users matching criteria
     */
    List<Usuario> findBySucursalIdAndRolNombreAndActivo(UUID sucursalId, String rolNombre, Boolean activo);

    /**
     * Lists active users by role for a specific branch.
     *
     * @param sucursalId branch identifier
     * @param rolNombre role name
     * @return active users
     */
    List<Usuario> findBySucursalIdAndRolNombreAndActivoTrue(UUID sucursalId, String rolNombre);

    /**
     * Checks if a specific active user exists for a branch and role.
     *
     * @param id user identifier
     * @param sucursalId branch identifier
     * @param rolNombre role name
     * @return true if user exists
     */
    boolean existsByIdAndSucursalIdAndRolNombreAndActivoTrue(UUID id, UUID sucursalId, String rolNombre);
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