package com.veloservice.finanzas.domain.model;

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

import com.veloservice.finanzas.domain.TipoDocumentoEnum;
import com.veloservice.finanzas.domain.MetodoPagoEnum;
import com.veloservice.finanzas.domain.EstadoCobroEnum;
import com.veloservice.finanzas.domain.EstadoSIIEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
/**
 * Cobro y datos tributarios asociados a una orden de trabajo.
 */
@Entity
@Table(
    name = "cobros",
    indexes = {
        @Index(name = "idx_cobros_orden", columnList = "orden_id")
},
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_cobros_orden", columnNames = {"orden_id"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cobro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumentoEnum tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "subtotal_servicios", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalServicios;

    @Column(name = "subtotal_productos", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotalProductos;

    @Column(name = "descuento_membresia", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoMembresia;

    @Column(name = "descuento_manual", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoManual;

    @Column(name = "neto", nullable = false, precision = 12, scale = 2)
    private BigDecimal neto;

    @Column(name = "iva", nullable = false, precision = 12, scale = 2)
    private BigDecimal iva;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPagoEnum metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCobroEnum estado;

    @Column(name = "folio_sii")
    private String folioSii;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_sii", nullable = false)
    private EstadoSIIEnum estadoSii;

    @Column(name = "fecha_pago")
    private OffsetDateTime fechaPago;

    @Column(name = "anulada_at")
    private OffsetDateTime anuladaAt;

    @Column(name = "motivo_anulacion", columnDefinition = "TEXT")
    private String motivoAnulacion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
