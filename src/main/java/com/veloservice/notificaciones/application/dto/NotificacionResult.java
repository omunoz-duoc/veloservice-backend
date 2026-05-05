package com.veloservice.notificaciones.application.dto;

import com.veloservice.config.enums.CanalNotificacionEnum;
import com.veloservice.config.enums.EstadoNotificacionEnum;
import com.veloservice.config.enums.TipoNotificacionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application result for notification queries.
 */
@Data
@Builder
@AllArgsConstructor
public class NotificacionResult {
    private UUID id;
    private UUID ordenId;
    private CanalNotificacionEnum canal;
    private TipoNotificacionEnum tipo;
    private String mensaje;
    private EstadoNotificacionEnum estado;
    private Integer intentos;
    private OffsetDateTime programadaPara;
    private OffsetDateTime enviadaAt;
    private OffsetDateTime createdAt;
}
