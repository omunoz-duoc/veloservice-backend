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
import java.util.UUID;

/**
 * Estado de flujo de una garantía asociada a una orden.
 */
@Entity
@Table(
    name = "estados_garantia",
    uniqueConstraints = {
        @UniqueConstraint(name = "codigo", columnNames = {"codigo"})
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoGarantia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "es_final", nullable = false)
    private Boolean esFinal;
}
