package com.veloservice.notificaciones.domain.model;

import com.veloservice.notificaciones.domain.CanalNotificacionEnum;
import com.veloservice.notificaciones.domain.EstadoNotificacionEnum;
import com.veloservice.notificaciones.domain.TipoNotificacionEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "orden_id", nullable = false)
    private UUID ordenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalNotificacionEnum canal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoNotificacionEnum tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoNotificacionEnum estado = EstadoNotificacionEnum.pendiente;

    @Column(nullable = false)
    @Builder.Default
    private Integer intentos = 0;

    @Column(name = "programada_para")
    private OffsetDateTime programadaPara;

    @Column(name = "enviada_at")
    private OffsetDateTime enviadaAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}