package com.bikeshop.manager.domain.tenant;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a payment settlement for a work order.
 */
@Entity
@Table(name = "cobros", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"orden_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cobro {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, length = 50)
    private String numeroDocumento;

    @Column(name = "subtotal_servicios", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalServicios;

    @Column(name = "subtotal_productos", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotalProductos;

    @Column(name = "descuento_membresia", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuentoMembresia = BigDecimal.ZERO;

    @Column(name = "descuento_manual", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuentoManual = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal neto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal iva;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "Pendiente";

    @Column(name = "folio_sii", length = 50)
    private String folioSii;

    @Column(name = "estado_sii", nullable = false, length = 20)
    @Builder.Default
    private String estadoSii = "pendiente";

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago;

    @Column(name = "anulada_at")
    private LocalDateTime anuladaAt;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;
}
