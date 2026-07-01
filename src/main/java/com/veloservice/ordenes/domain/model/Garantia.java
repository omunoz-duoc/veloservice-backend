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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.veloservice.ordenes.domain.EstadoGarantiaEnum;
import jakarta.persistence.Transient;
/**
 * Garantía abierta a partir de una orden entregada y su resolución.
 */
@Entity
@Table(
    name = "garantias",
    indexes = {
        @Index(name = "idx_garantias_taller", columnList = "taller_id"),
        @Index(name = "idx_garantias_orden", columnList = "orden_id"),
        @Index(name = "idx_garantias_estado", columnList = "estado_id"),
        @Index(name = "idx_garantias_taller_numero", columnList = "taller_id, numero_garantia", unique = true)
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Garantia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "estado_id", nullable = false)
    private UUID estadoId;

    @Column(name = "numero_garantia", nullable = false)
    private String numeroGarantia;

    @Column(name = "marca_bicicleta")
    private String marcaBicicleta;

    @Column(name = "componente_afectado")
    private String componenteAfectado;

    @Column(name = "descripcion_falla", columnDefinition = "TEXT")
    private String descripcionFalla;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "condiciones", columnDefinition = "TEXT")
    private String condiciones;

    @Column(name = "resolucion", columnDefinition = "TEXT")
    private String resolucion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Transient
    private EstadoGarantiaEnum estado;
}
