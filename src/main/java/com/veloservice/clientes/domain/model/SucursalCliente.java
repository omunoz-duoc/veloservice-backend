package com.veloservice.clientes.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents the link between a sucursal (branch) and a customer.
 * Maps to sucursal_clientes table.
 */
@Entity
@Table(name = "sucursal_clientes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sucursal_id", "cliente_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SucursalCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "membresia_id")
    private UUID membresiaId;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "membresia_desde")
    private OffsetDateTime membresiaDesde;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}