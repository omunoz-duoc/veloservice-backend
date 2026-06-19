package com.veloservice.administracion.domain.model;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Plan comercial del SaaS que define las condiciones disponibles para un taller.
 */
@Entity
@Table(
    name = "planes_saas",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_planes_saas_codigo", columnNames = {"codigo"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanSaas {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "activo", nullable = false)
    private Boolean activo;

    @Builder.Default
    @Column(name = "max_sucursales", nullable = false)
    private Integer maxSucursales = 1;

    @Builder.Default
    @Column(name = "max_usuarios", nullable = false)
    private Integer maxUsuarios = 1;

    @Column(name = "max_ordenes_mes")
    private Integer maxOrdenesMes;

    @Builder.Default
    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual = BigDecimal.ZERO;

    @Column(name = "precio_anual", precision = 10, scale = 2)
    private BigDecimal precioAnual;

    @Builder.Default
    @Column(name = "trial_dias", nullable = false)
    private Integer trialDias = 0;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "features", nullable = false)
    private String features = "{}";
}
