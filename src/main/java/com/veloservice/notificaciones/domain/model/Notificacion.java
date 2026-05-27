package com.veloservice.notificaciones.domain.model;

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
import java.time.OffsetDateTime;
import java.util.UUID;

import com.veloservice.notificaciones.domain.CanalNotificacionEnum;
import com.veloservice.notificaciones.domain.TipoNotificacionEnum;
import com.veloservice.notificaciones.domain.EstadoNotificacionEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
/**
 * Notificación programada o enviada respecto de una orden.
 */
@Entity
@Table(
    name = "notificaciones",
    indexes = {
        @Index(name = "idx_notificaciones_orden", columnList = "orden_id"),
        @Index(name = "idx_notificaciones_estado", columnList = "estado")
}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false)
    private CanalNotificacionEnum canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoNotificacionEnum tipo;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoNotificacionEnum estado;

    @Column(name = "intentos", nullable = false)
    private Integer intentos;

    @Column(name = "programada_para")
    private OffsetDateTime programadaPara;

    @Column(name = "enviada_at")
    private OffsetDateTime enviadaAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
