package com.veloservice.notificaciones.interfaces.rest.dto;

import com.veloservice.notificaciones.domain.CanalNotificacionEnum;
import com.veloservice.notificaciones.domain.TipoNotificacionEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Notification creation request payload.
 */
@Data
public class NotificacionRequest {
    @NotNull
    private UUID ordenId;
    @NotNull
    private CanalNotificacionEnum canal;
    @NotNull
    private TipoNotificacionEnum tipo;
    @NotBlank
    private String mensaje;
    private OffsetDateTime programadaPara;
}
