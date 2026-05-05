package com.veloservice.ordenes.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a product line within a work order.
 */
@Entity
@Table(name = "orden_productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_costo_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCostoSnapshot;

    @Column(name = "precio_venta_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVentaSnapshot;

    @Column(name = "precio_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioAplicado;

    @Column(name = "proporcionado_por_cliente", nullable = false)
    @Builder.Default
    private Boolean proporcionadoPorCliente = false;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}