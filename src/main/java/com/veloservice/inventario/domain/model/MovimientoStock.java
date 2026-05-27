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

import com.veloservice.inventario.domain.TipoMovimientoEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
/**
 * Movimiento que audita entradas, salidas, ajustes y devoluciones de stock.
 */
@Entity
@Table(
    name = "movimientos_stock",
    indexes = {
        @Index(name = "idx_mov_stock_producto", columnList = "producto_id"),
        @Index(name = "idx_mov_stock_orden", columnList = "orden_id"),
        @Index(name = "idx_mov_stock_compra", columnList = "compra_id")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "orden_id")
    private UUID ordenId;

    @Column(name = "compra_id")
    private UUID compraId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "traslado_id")
    private UUID trasladoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMovimientoEnum tipo;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_posterior", nullable = false)
    private Integer stockPosterior;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
