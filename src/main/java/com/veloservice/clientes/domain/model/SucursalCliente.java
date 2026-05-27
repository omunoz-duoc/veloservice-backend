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

/**
 * Relación histórica entre sucursal y cliente; no forma parte de schema_v3, pero se conserva por compatibilidad del repositorio.
 */
@Entity
@Table(
    name = "sucursal_clientes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sucursal_clientes_sucursal_cliente", columnNames = {"sucursal_id", "cliente_id"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SucursalCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "membresia_id")
    private UUID membresiaId;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "membresia_desde")
    private OffsetDateTime membresiaDesde;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
