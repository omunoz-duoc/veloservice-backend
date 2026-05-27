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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Producto de inventario con stock controlado a nivel de sucursal.
 */
@Entity
@Table(
    name = "productos",
    indexes = {
        @Index(name = "idx_productos_sucursal", columnList = "sucursal_id"),
        @Index(name = "idx_productos_categoria", columnList = "categoria_id"),
        @Index(name = "idx_productos_sku", columnList = "sku"),
        @Index(name = "idx_productos_sucursal_sku", columnList = "sucursal_id, sku", unique = true)
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "categoria_id")
    private UUID categoriaId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "sku")
    private String sku;

    @Column(name = "marca")
    private String marca;

    @Column(name = "unidad_medida", nullable = false)
    private String unidadMedida;

    @Column(name = "precio_costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCosto;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
