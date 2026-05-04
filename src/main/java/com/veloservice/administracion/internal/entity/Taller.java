package com.veloservice.administracion.internal.entity;

import com.veloservice.config.enums.PlanSaasEnum;
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
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a bike workshop.
 */
@Entity
@Table(name = "talleres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Taller {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false, unique = true)
    private String rut;
    private String telefono;
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(name = "plan_saas", nullable = false)
    @Builder.Default
    private PlanSaasEnum planSaas = PlanSaasEnum.basico;
    @Column(name = "logo_url")
    private String logoUrl;
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}