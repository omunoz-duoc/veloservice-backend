package com.veloservice.notificaciones.interfaces.mapper;

import com.veloservice.notificaciones.application.dto.NotificacionCreateCommand;
import com.veloservice.notificaciones.application.dto.NotificacionResult;
import com.veloservice.notificaciones.interfaces.rest.NotificacionRequest;
import com.veloservice.notificaciones.interfaces.rest.NotificacionResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class NotificacionMapper {
    private NotificacionMapper() {
    }

    public static NotificacionCreateCommand toCommand(NotificacionRequest request) {
        return new NotificacionCreateCommand(
                request.getOrdenId(),
                request.getCanal(),
                request.getTipo(),
                request.getMensaje(),
                request.getProgramadaPara()
        );
    }

    public static NotificacionResponse toResponse(NotificacionResult result) {
        return NotificacionResponse.builder()
                .id(result.getId())
                .ordenId(result.getOrdenId())
                .canal(result.getCanal())
                .tipo(result.getTipo())
                .mensaje(result.getMensaje())
                .estado(result.getEstado())
                .intentos(result.getIntentos())
                .programadaPara(result.getProgramadaPara())
                .enviadaAt(result.getEnviadaAt())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public static List<NotificacionResponse> toResponseList(List<NotificacionResult> results) {
        return results.stream()
                .map(NotificacionMapper::toResponse)
                .collect(Collectors.toList());
    }
}
