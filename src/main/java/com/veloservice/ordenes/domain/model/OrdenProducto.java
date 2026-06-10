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
 * Producto utilizado o registrado dentro de una orden de trabajo.
 */
@Entity
@Table(
    name = "orden_productos",
    indexes = {
        @Index(name = "idx_orden_productos_orden", columnList = "orden_id"),
        @Index(name = "idx_orden_productos_producto", columnList = "producto_id")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "precio_costo_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCostoSnapshot;

    @Column(name = "precio_venta_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVentaSnapshot;

    @Column(name = "precio_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioAplicado;

    @Column(name = "proporcionado_por_cliente", nullable = false)
    private Boolean proporcionadoPorCliente;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
