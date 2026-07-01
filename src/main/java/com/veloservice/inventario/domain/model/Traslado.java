package com.veloservice.inventario.domain.model;

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
 * Traslado de stock entre sucursales de un mismo taller.
 */
@Entity
@Table(name = "traslados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Traslado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "sucursal_origen", nullable = false)
    private UUID sucursalOrigen;

    @Column(name = "sucursal_destino", nullable = false)
    private UUID sucursalDestino;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
