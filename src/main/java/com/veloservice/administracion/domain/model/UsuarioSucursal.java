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
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Asignación de un usuario operativo a una sucursal en la que puede trabajar.
 */
@Entity
@Table(
    name = "usuario_sucursales",
    indexes = {
        @Index(name = "idx_usuario_sucursales_usuario", columnList = "usuario_id"),
        @Index(name = "idx_usuario_sucursales_sucursal", columnList = "sucursal_id")
},
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_sucursales_usuario_sucursal", columnNames = {"usuario_id", "sucursal_id"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "es_principal", nullable = false)
    private Boolean esPrincipal;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
