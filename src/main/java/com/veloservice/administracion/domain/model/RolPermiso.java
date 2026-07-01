package com.veloservice.administracion.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

/**
 * Permiso de un rol sobre un módulo para controlar acciones disponibles.
 */
@Entity
@Table(
    name = "rol_permisos",
    indexes = {
        @Index(name = "idx_rol_permisos_rol", columnList = "rol_id"),
        @Index(name = "idx_rol_permisos_modulo", columnList = "modulo_id")
},
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_rol_permisos_rol_modulo", columnNames = {"rol_id", "modulo_id"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "rol_id", nullable = false)
    private UUID rolId;

    @Column(name = "modulo_id", nullable = false)
    private UUID moduloId;

    @Column(name = "puede_ver", nullable = false)
    private Boolean puedeVer;

    @Column(name = "puede_crear", nullable = false)
    private Boolean puedeCrear;

    @Column(name = "puede_editar", nullable = false)
    private Boolean puedeEditar;

    @Column(name = "puede_eliminar", nullable = false)
    private Boolean puedeEliminar;
}
