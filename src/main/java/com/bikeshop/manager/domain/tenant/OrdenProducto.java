package com.bikeshop.manager.domain.tenant;

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

import java.math.BigDecimal;
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

    @Column(name = "precio_costo_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioCostoSnapshot;

    @Column(name = "precio_venta_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVentaSnapshot;

    @Column(name = "precio_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAplicado;

    @Column(name = "proporcionado_por_cliente", nullable = false)
    @Builder.Default
    private Boolean proporcionadoPorCliente = false;

    @Column(columnDefinition = "TEXT")
    private String notas;
}
