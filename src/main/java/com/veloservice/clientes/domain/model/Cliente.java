package com.veloservice.clientes.domain.model;

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

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
/**
 * Cliente perteneciente a un taller, visible por todas sus sucursales.
 */
@Entity
@Table(
    name = "clientes",
    indexes = {
        @Index(name = "idx_clientes_taller", columnList = "taller_id"),
        @Index(name = "idx_clientes_membresia", columnList = "membresia_id"),
        @Index(name = "idx_clientes_taller_codigo_cliente", columnList = "taller_id, codigo_cliente", unique = true),
        @Index(name = "idx_clientes_taller_rut", columnList = "taller_id, rut", unique = true)
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "codigo_cliente", nullable = false, length = 20)
    private String codigoCliente;

    @Column(name = "membresia_id")
    private UUID membresiaId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "rut")
    private String rut;

    @Column(name = "telefono")
    private String telefono;

    @Column(name = "email")
    private String email;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "membresia_desde")
    private OffsetDateTime membresiaDesde;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Bicicleta> bicicletas = new ArrayList<>();
}
