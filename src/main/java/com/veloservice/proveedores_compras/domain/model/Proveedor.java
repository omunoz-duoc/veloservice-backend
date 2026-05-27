package com.veloservice.proveedores_compras.domain.model;

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
 * Proveedor del taller usado para compras y abastecimiento.
 */
@Entity
@Table(
    name = "proveedores",
    indexes = {
        @Index(name = "idx_proveedores_taller", columnList = "taller_id")
},
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_proveedores_taller_nombre", columnNames = {"taller_id", "nombre"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "rut")
    private String rut;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "email")
    private String email;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
