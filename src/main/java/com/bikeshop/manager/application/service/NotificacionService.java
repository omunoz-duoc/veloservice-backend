package com.bikeshop.manager.application.service;

import com.bikeshop.manager.domain.tenant.Notificacion;
import com.bikeshop.manager.infrastructure.persistence.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Transactional
    public Notificacion crear(Notificacion n) {
        n.setEstado("pendiente");
        n.setIntentos(0);
        return notificacionRepository.save(n);
    }

    @Transactional
    public Notificacion enviar(UUID id) {
        Notificacion n = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificacion no encontrada"));

        // Simular envío: marcar enviada y registrar timestamp
        n.setIntentos(n.getIntentos() + 1);
        n.setEstado("enviada");
        n.setEnviadaAt(LocalDateTime.now());
        return notificacionRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listarPorOrden(UUID ordenId) {
        return notificacionRepository.findByOrdenId(ordenId);
    }
}
