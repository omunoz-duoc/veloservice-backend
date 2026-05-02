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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a work order within a tenant.
 */
@Entity
@Table(name = "ordenes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"taller_id", "numero_orden"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "bicicleta_id", nullable = false)
    private UUID bicicletaId;

    @Column(name = "mecanico_id", nullable = false)
    private UUID mecanicoId;

    @Column(name = "numero_orden", nullable = false, length = 30)
    private String numeroOrden;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "diagnostico_inicial", columnDefinition = "TEXT")
    private String diagnosticoInicial;

    @Column(name = "diagnostico_final", columnDefinition = "TEXT")
    private String diagnosticoFinal;

    @Column(name = "observaciones_cliente", columnDefinition = "TEXT")
    private String observacionesCliente;

    @Column(name = "descuento_manual", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuentoManual = BigDecimal.ZERO;

    @Column(name = "porcentaje_descuento_membresia", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuentoMembresia = BigDecimal.ZERO;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_prometida")
    private LocalDateTime fechaPrometida;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
