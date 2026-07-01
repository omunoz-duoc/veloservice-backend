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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Servicio cobrado o aplicado dentro de una orden de trabajo.
 */
@Entity
@Table(
    name = "orden_servicios",
    indexes = {
        @Index(name = "idx_orden_servicios_orden", columnList = "orden_id")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "servicio_id", nullable = false)
    private UUID servicioId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "precio_base_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioBaseSnapshot;

    @Column(name = "precio_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioAplicado;

    @Column(name = "descuento_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoAplicado;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
