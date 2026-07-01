package com.veloservice.proveedores_compras.domain.model;

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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.veloservice.proveedores_compras.domain.EstadoCompraEnum;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
/**
 * Compra registrada para una relación sucursal-proveedor y su estado de recepción.
 */
@Entity
@Table(
    name = "compras",
    indexes = {
        @Index(name = "idx_compras_sucursal_proveedor", columnList = "sucursal_proveedor_id"),
        @Index(name = "idx_compras_usuario", columnList = "usuario_id"),
        @Index(name = "idx_compras_estado", columnList = "estado_id")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "sucursal_proveedor_id", nullable = false)
    private UUID sucursalProveedorId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "estado_id", nullable = false)
    private UUID estadoId;

    @Column(name = "numero_factura")
    private String numeroFactura;

    @Column(name = "neto", nullable = false, precision = 12, scale = 2)
    private BigDecimal neto;

    @Column(name = "iva", nullable = false, precision = 12, scale = 2)
    private BigDecimal iva;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "fecha_compra")
    private LocalDate fechaCompra;

    @Column(name = "fecha_recepcion")
    private LocalDate fechaRecepcion;

    @Column(name = "notas", columnDefinition = "TEXT")
    private String notas;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Transient
    private EstadoCompraEnum estado;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CompraProducto> lineas = new ArrayList<>();
}
