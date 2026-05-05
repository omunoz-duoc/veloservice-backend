package com.veloservice.notificaciones.application.dto;

import com.veloservice.config.enums.CanalNotificacionEnum;
import com.veloservice.config.enums.TipoNotificacionEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Application command for creating a notification.
 */
@Data
@AllArgsConstructor
public class NotificacionCreateCommand {
    private UUID ordenId;
    private CanalNotificacionEnum canal;
    private TipoNotificacionEnum tipo;
    private String mensaje;
    private OffsetDateTime programadaPara;
}
