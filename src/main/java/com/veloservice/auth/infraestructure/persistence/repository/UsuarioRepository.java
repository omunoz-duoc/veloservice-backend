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

    Optional<Usuario> findByEmailAndActivoTrue(String email);

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByIdAndActivoTrueAndRol_Nombre(UUID id, String rolNombre);

    // List<Usuario> findBySucursalIdAndActivoTrue(UUID sucursalId);

    // List<Usuario> findBySucursalIdAndRolNombre(UUID sucursalId, String rolNombre);

    // List<Usuario> findBySucursalIdAndRolNombreAndActivo(UUID sucursalId, String rolNombre, Boolean activo);

    // List<Usuario> findBySucursalIdAndRolNombreAndActivoTrue(UUID sucursalId, String rolNombre);

    // boolean existsByIdAndSucursalIdAndRolNombreAndActivoTrue(UUID id, UUID sucursalId, String rolNombre);

    // @Query("""
    //         select u
    //         from Usuario u
    //         join fetch u.rol r
    //         join fetch u.sucursal s
    //         where upper(r.nombre) = 'MECANICO'
    //             and s.id = :sucursalId
    //             and (:activo is null or u.activo = :activo)
    //         order by u.apellido asc, u.nombre asc
    //         """)
    // List<Usuario> findMecanicosBySucursalIdAndActivo(@Param("sucursalId") UUID sucursalId,
    //                                                  @Param("activo") Boolean activo);

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
