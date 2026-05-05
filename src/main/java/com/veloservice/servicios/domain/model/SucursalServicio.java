package com.veloservice.servicios.domain.model;

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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents the link between a sucursal (branch) and a service.
 * Allows customizing service prices per branch.
 * Maps to sucursal_servicios table.
 */
@Entity
@Table(name = "sucursal_servicios", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"sucursal_id", "servicio_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SucursalServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "servicio_id", nullable = false)
    private UUID servicioId;

    @Column(name = "precio_personalizado", precision = 12, scale = 2)
    private BigDecimal precioPersonalizado;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}