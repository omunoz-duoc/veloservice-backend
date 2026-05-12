package com.veloservice.ordenes.domain.model;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.TipoOrdenEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Represents a work order within a tenant.
 */
@Entity
@Table(name = "ordenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "external_id", nullable = false, unique = true)
    private String externalId;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "bicicleta_id", nullable = false)
    private UUID bicicletaId;

    @Column(name = "mecanico_id")
    private UUID mecanicoId;

    @Column(name = "numero_orden", nullable = false, unique = true)
    private String numeroOrden;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "estado_orden_enum")
    @Builder.Default
    private EstadoOrdenEnum estado = EstadoOrdenEnum.recibida;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "tipo_orden_enum")
    @Builder.Default
    private TipoOrdenEnum tipo = TipoOrdenEnum.reparacion;

    @Column(name = "diagnostico_inicial", columnDefinition = "TEXT")
    private String diagnosticoInicial;

    @Column(name = "diagnostico_final", columnDefinition = "TEXT")
    private String diagnosticoFinal;

    @Column(name = "observaciones_cliente", columnDefinition = "TEXT")
    private String observacionesCliente;

    @Column(name = "descuento_manual", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuentoManual = BigDecimal.ZERO;

    @Column(name = "porcentaje_descuento_membresia", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuentoMembresia = BigDecimal.ZERO;

    @Column(name = "fecha_ingreso", nullable = false)
    private OffsetDateTime fechaIngreso;

    @Column(name = "fecha_prometida")
    private OffsetDateTime fechaPrometida;

    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}