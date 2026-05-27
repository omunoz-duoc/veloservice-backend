package com.veloservice.auth.domain.model;

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
import java.time.OffsetDateTime;
import java.util.UUID;

import com.veloservice.administracion.domain.model.Sucursal;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
/**
 * Usuario operativo que pertenece a un taller y accede según su rol y sucursales asignadas.
 */
@Entity
@Table(
    name = "usuarios",
    indexes = {
        @Index(name = "idx_usuarios_taller", columnList = "taller_id"),
        @Index(name = "idx_usuarios_rol", columnList = "rol_id")
},
    uniqueConstraints = {
        @UniqueConstraint(name = "email", columnNames = {"email"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "rol_id", nullable = false)
    private UUID rolId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "rut")
    private String rut;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rol_id", insertable = false, updatable = false)
    private Rol rol;

    @Transient
    private UUID sucursalId;

    @Transient
    private Sucursal sucursal;
}
