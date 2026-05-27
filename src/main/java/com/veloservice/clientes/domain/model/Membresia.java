package com.veloservice.clientes.domain.model;

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
import java.util.UUID;

/**
 * Nivel de membresía definido por un taller para beneficios de sus clientes.
 */
@Entity
@Table(
    name = "membresias",
    indexes = {
        @Index(name = "idx_membresias_taller", columnList = "taller_id")
},
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_membresias_taller_nombre", columnNames = {"taller_id", "nombre"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "taller_id", nullable = false)
    private UUID tallerId;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "porcentaje_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeDescuento;

    @Column(name = "prioridad_atencion", nullable = false)
    private Integer prioridadAtencion;

    @Column(name = "color_badge")
    private String colorBadge;

    @Column(name = "activo", nullable = false)
    private Boolean activo;
}
