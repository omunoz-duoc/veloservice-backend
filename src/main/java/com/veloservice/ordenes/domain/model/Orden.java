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

import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.ordenes.domain.TipoOrdenEnum;
import jakarta.persistence.Transient;
/**
 * Orden de trabajo de una bicicleta, asociada a taller, sucursal, estado y tipo.
 */
@Entity
@Table(
    name = "ordenes",
    indexes = {
        @Index(name = "idx_ordenes_taller", columnList = "taller_id"),
        @Index(name = "idx_ordenes_sucursal", columnList = "sucursal_id"),
        @Index(name = "idx_ordenes_bicicleta", columnList = "bicicleta_id"),
        @Index(name = "idx_ordenes_mecanico", columnList = "mecanico_id"),
        @Index(name = "idx_ordenes_estado", columnList = "estado_id"),
        @Index(name = "idx_ordenes_tipo", columnList = "tipo_id"),
        @Index(name = "idx_ordenes_taller_numero", columnList = "taller_id, numero_orden", unique = true)
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "sucursal_id", nullable = false)
    private UUID sucursalId;

    @Column(name = "bicicleta_id", nullable = false)
    private UUID bicicletaId;

    @Column(name = "mecanico_id")
    private UUID mecanicoId;

    @Column(name = "estado_id", nullable = false)
    private UUID estadoId;

    @Column(name = "tipo_id", nullable = false)
    private UUID tipoId;

    @Column(name = "numero_orden", nullable = false)
    private String numeroOrden;

    @Column(name = "diagnostico_inicial", columnDefinition = "TEXT")
    private String diagnosticoInicial;

    @Column(name = "diagnostico_final", columnDefinition = "TEXT")
    private String diagnosticoFinal;

    @Column(name = "observaciones_cliente", columnDefinition = "TEXT")
    private String observacionesCliente;

    @Column(name = "descuento_manual", nullable = false, precision = 12, scale = 2)
    private BigDecimal descuentoManual;

    @Column(name = "porcentaje_descuento_membresia", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuentoMembresia;

    @Column(name = "prioridad")
    private String prioridad;

    @Column(name = "fecha_ingreso", nullable = false)
    private OffsetDateTime fechaIngreso;

    @Column(name = "fecha_prometida")
    private OffsetDateTime fechaPrometida;

    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Transient
    private String externalId;

    @Transient
    private UUID mecanicoAsignadoId;

    @Transient
    private EstadoOrdenEnum estado;

    @Transient
    private TipoOrdenEnum tipo;

    @Transient
    private String descripcionTrabajo;
}
