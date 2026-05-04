package com.veloservice.administracion.internal.entity;

import com.veloservice.administracion.internal.entity.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "rol_permisos", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"rol_id", "modulo_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolPermiso {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @Column(name = "puede_ver", nullable = false)
    @Builder.Default
    private Boolean puedeVer = false;

    @Column(name = "puede_crear", nullable = false)
    @Builder.Default
    private Boolean puedeCrear = false;

    @Column(name = "puede_editar", nullable = false)
    @Builder.Default
    private Boolean puedeEditar = false;

    @Column(name = "puede_eliminar", nullable = false)
    @Builder.Default
    private Boolean puedeEliminar = false;
}