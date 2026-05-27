package com.veloservice.ordenes.domain.model;

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

import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import jakarta.persistence.Transient;
/**
 * Registro histórico de cambios de estado de una orden de trabajo.
 */
@Entity
@Table(
    name = "orden_estados",
    indexes = {
        @Index(name = "idx_orden_estados_orden", columnList = "orden_id")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenEstado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "estado_anterior_id")
    private UUID estadoAnteriorId;

    @Column(name = "estado_nuevo_id", nullable = false)
    private UUID estadoNuevoId;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Transient
    private EstadoOrdenEnum estadoAnterior;

    @Transient
    private EstadoOrdenEnum estadoNuevo;
}
