package com.veloservice.finanzas.internal.entity;

import com.veloservice.config.enums.EstadoCobroEnum;
import com.veloservice.config.enums.EstadoSIIEnum;
import com.veloservice.config.enums.MetodoPagoEnum;
import com.veloservice.config.enums.TipoDocumentoEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    @Builder.Default
    private TipoDocumentoEnum tipoDocumento = TipoDocumentoEnum.boleta;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "subtotal_servicios", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalServicios;

    @Column(name = "subtotal_productos", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalProductos;

    @Column(name = "descuento_membresia", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuentoMembresia = BigDecimal.ZERO;

    @Column(name = "descuento_manual", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuentoManual = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal neto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal iva;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    @Builder.Default
    private MetodoPagoEnum metodoPago = MetodoPagoEnum.efectivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoCobroEnum estado = EstadoCobroEnum.pendiente;

    @Column(name = "folio_sii")
    private String folioSii;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sii", nullable = false)
    @Builder.Default
    private EstadoSIIEnum estadoSii = EstadoSIIEnum.no_aplica;

    @Column(name = "fecha_pago")
    private OffsetDateTime fechaPago;

    @Column(name = "anulada_at")
    private OffsetDateTime anuladaAt;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}