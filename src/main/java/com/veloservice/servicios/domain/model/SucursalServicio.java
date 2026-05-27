package com.veloservice.servicios.domain.model;

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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Precio personalizado de un servicio específico para una sucursal.
 */
@Entity
@Table(
    name = "sucursal_servicios",
    indexes = {
        @Index(name = "idx_sucursal_servicios_sucursal", columnList = "sucursal_id"),
        @Index(name = "idx_sucursal_servicios_servicio", columnList = "servicio_id")
},
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sucursal_servicios_sucursal_servicio", columnNames = {"sucursal_id", "servicio_id"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SucursalServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "servicio_id", nullable = false)
    private UUID servicioId;

    @Column(name = "precio_personalizado", precision = 12, scale = 2)
    private BigDecimal precioPersonalizado;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
