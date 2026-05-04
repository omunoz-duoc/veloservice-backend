package com.veloservice.taller.internal.entity;

import com.veloservice.config.enums.EstadoGarantiaEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a warranty for a work order product item.
 */
@Entity
@Table(name = "garantias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Garantia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "numero_garantia", nullable = false, length = 50, unique = true)
    private String numeroGarantia;

    @Column(name = "marca_bicicleta")
    private String marcaBicicleta;

    @Column(name = "componente_afectado")
    private String componenteAfectado;

    @Column(name = "descripcion_falla", columnDefinition = "TEXT")
    private String descripcionFalla;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoGarantiaEnum estado = EstadoGarantiaEnum.abierta;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(columnDefinition = "TEXT")
    private String condiciones;

    @Column(columnDefinition = "TEXT")
    private String resolucion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}