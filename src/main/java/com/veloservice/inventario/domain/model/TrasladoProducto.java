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
import java.util.UUID;

/**
 * Producto y cantidad incluidos en un traslado de stock.
 */
@Entity
@Table(
    name = "traslado_productos",
    indexes = {
        @Index(name = "idx_traslado_productos_traslado", columnList = "traslado_id"),
        @Index(name = "idx_traslado_productos_producto", columnList = "producto_id")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrasladoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "traslado_id", nullable = false)
    private UUID trasladoId;

    @Column(name = "producto_id", nullable = false)
    private UUID productoId;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
