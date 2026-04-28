package com.bikeshop.manager.domain.tenant;

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

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the link between a tenant and a customer.
 */
@Entity
@Table(name = "taller_clientes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"taller_id", "cliente_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TallerCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "membresia_id")
    private UUID membresiaId;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @Column(name = "membresia_desde")
    private LocalDateTime membresiaDesde;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
