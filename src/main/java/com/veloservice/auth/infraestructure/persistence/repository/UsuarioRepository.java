package com.veloservice.auth.infraestructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.veloservice.auth.domain.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

  /**
   * Busca un usuario activo por su email.
   * @param email
   * @return
   */
    Optional<Usuario> findByEmailAndActivoTrue(String email);

    /**
     * Busca un usuario por su email, sin importar si está activo o no.
     * @param email
     * @return
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * Verifica si existe un usuario activo con el email dado.
     * @param email
     * @return
     */
    boolean existsByEmail(String email);

    @Query("""
            select count(u) > 0
            from Usuario u
            where upper(replace(replace(replace(u.rut, '.', ''), '-', ''), ' ', '')) = :rut
            """)
    boolean existsByNormalizedRut(@Param("rut") String rut);

    /**
     * Verifica si existe un usuario activo con el ID dado y rol de mecánico.
     * @param id
     * @return
     */
    @Query("""
            select count(u) > 0
            from Usuario u
            join u.rol r
            where u.id = :id
              and u.activo = true
              and lower(r.nombre) = 'mecanico'
            """)
    boolean existsActiveMecanicoById(@Param("id") UUID id);

    /**
     * Busca todos los usuarios activos con rol de mecánico asociados a un taller específico.
     * @param tallerId
     * @return
     */
    @Query("""
            select u
            from Usuario u
            join fetch u.rol r
            where u.activo = true
              and lower(r.nombre) = 'mecanico'
              and u.tallerId = :tallerId
            order by u.apellido asc, u.nombre asc
            """)
    List<Usuario> findActiveMecanicosByTallerId(@Param("tallerId") UUID tallerId);

    /**
     * Busca todos los usuarios activos con rol de mecánico asociados a una sucursal específica.
     * @param sucursalId
     * @return
     */
    @Query("""
            select distinct u
            from Usuario u
            join fetch u.rol r
            where u.activo = true
              and lower(r.nombre) = 'mecanico'
              and exists (
                  select 1
                  from com.veloservice.administracion.domain.model.UsuarioSucursal us
                  where us.usuarioId = u.id
                    and us.sucursalId = :sucursalId
              )
            order by u.apellido asc, u.nombre asc
            """)
    List<Usuario> findActiveMecanicosBySucursalId(@Param("sucursalId") UUID sucursalId);


    /**
     * Verifica si existe un usuario activo con el ID dado y rol específico.
     * @param id
     * @param rolNombre
     * @return
     */
    boolean existsByIdAndActivoTrueAndRol_Nombre(UUID id, String rolNombre);


    /**
     * Busca todos los usuarios activos con rol de mecánico asociados a una sucursal específica.
     * @param sucursalId
     * @return
     */
    @Query("""
            SELECT u FROM Usuario u
            JOIN u.rol r
            WHERE r.nombre = 'mecanico'
              AND u.activo = true
              AND u.id IN (
                  SELECT us.usuarioId FROM UsuarioSucursal us
                  WHERE us.sucursalId = :sucursalId
              )
            ORDER BY u.apellido ASC, u.nombre ASC
            """)
    List<Usuario> findMecanicosBySucursalId(@Param("sucursalId") UUID sucursalId);
}
