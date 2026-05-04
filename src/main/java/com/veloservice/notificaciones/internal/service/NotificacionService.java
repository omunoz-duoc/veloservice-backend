package com.veloservice.notificaciones.internal.service;

import com.veloservice.config.enums.EstadoNotificacionEnum;
import com.veloservice.notificaciones.internal.entity.Notificacion;
import com.veloservice.notificaciones.internal.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional
    public Notificacion crear(Notificacion n) {
        n.setEstado(EstadoNotificacionEnum.pendiente);
        n.setIntentos(0);
        return notificacionRepository.save(n);
    }

    @Transactional
    public Notificacion enviar(UUID id) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificacion no encontrada"));

        // Simular envío: marcar enviada y registrar timestamp
        n.setIntentos(n.getIntentos() + 1);
        n.setEstado(EstadoNotificacionEnum.enviada);
        n.setEnviadaAt(OffsetDateTime.now());
        return notificacionRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarPorOrden(UUID ordenId) {
        return notificacionRepository.findByOrdenId(ordenId);
    }
}