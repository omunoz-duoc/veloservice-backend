package com.veloservice.clientes.domain.model;

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
 * Represents a membership tier for a tenant.
 */
@Entity
@Table(name = "membresias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membresia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeDescuento = BigDecimal.ZERO;

    @Column(name = "prioridad_atencion", nullable = false)
    @Builder.Default
    private Integer prioridadAtencion = 0;

    @Column(name = "color_badge")
    private String colorBadge;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}