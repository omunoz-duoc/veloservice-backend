package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.domain.model.Comentario;
import com.veloservice.ordenes.infraestructure.persistence.repository.ComentarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.veloservice.ordenes.interfaces.rest.ComentarioRequest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ComentarioResult> listarPorOrden(UUID ordenId) {
        return comentarioRepository.findByOrdenIdOrderByCreatedAtAsc(ordenId)
                .stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    public List<ComentarioResult> listarComentariosPorOrden(UUID ordenId) {
        return listarPorOrden(ordenId);
    }

    @Transactional
    public ComentarioResult agregar(UUID ordenId, String texto) {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) throw new IllegalStateException("Usuario no presente en contexto");

        Comentario comentario = Comentario.builder()
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .texto(texto)
                .build();

        return toResult(comentarioRepository.save(comentario));
    }

    public ComentarioResult agregarComentario(UUID ordenId, ComentarioRequest request) {
        return agregar(ordenId, request.getTexto());
    }

    private ComentarioResult toResult(Comentario c) {
        String autor = usuarioRepository.findById(c.getUsuarioId())
                .map(u -> u.getNombre() + " " + u.getApellido())
                .orElse("Desconocido");
        return ComentarioResult.builder()
                .id(c.getId())
                .autor(autor)
                .texto(c.getTexto())
                .creadoEn(c.getCreatedAt())
                .build();
    }
}
