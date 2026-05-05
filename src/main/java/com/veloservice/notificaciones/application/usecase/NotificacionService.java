package com.veloservice.notificaciones.application.usecase;

import com.veloservice.config.enums.EstadoNotificacionEnum;
import com.veloservice.notificaciones.application.dto.NotificacionCreateCommand;
import com.veloservice.notificaciones.application.dto.NotificacionResult;
import com.veloservice.notificaciones.domain.model.Notificacion;
import com.veloservice.notificaciones.infraestructure.persistence.repository.NotificacionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional
    public NotificacionResult crear(NotificacionCreateCommand command) {
        Notificacion n = Notificacion.builder()
                .ordenId(command.getOrdenId())
                .canal(command.getCanal())
                .tipo(command.getTipo())
                .mensaje(command.getMensaje())
                .programadaPara(command.getProgramadaPara())
                .build();
        n.setEstado(EstadoNotificacionEnum.pendiente);
        n.setIntentos(0);
        return toResult(notificacionRepository.save(n));
    }

    @Transactional
    public NotificacionResult enviar(UUID id) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificacion no encontrada"));

        // Simular envío: marcar enviada y registrar timestamp
        n.setIntentos(n.getIntentos() + 1);
        n.setEstado(EstadoNotificacionEnum.enviada);
        n.setEnviadaAt(OffsetDateTime.now());
        return toResult(notificacionRepository.save(n));
    }

    @Transactional(readOnly = true)
    public List<NotificacionResult> listarPorOrden(UUID ordenId) {
        return notificacionRepository.findByOrdenId(ordenId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private NotificacionResult toResult(Notificacion n) {
        return NotificacionResult.builder()
                .id(n.getId())
                .ordenId(n.getOrdenId())
                .canal(n.getCanal())
                .tipo(n.getTipo())
                .mensaje(n.getMensaje())
                .estado(n.getEstado())
                .intentos(n.getIntentos())
                .programadaPara(n.getProgramadaPara())
                .enviadaAt(n.getEnviadaAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}