package com.veloservice.ordenes.application.usecase;

import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.domain.model.OrdenComentario;
import com.veloservice.ordenes.infraestructure.persistence.repository.ComentarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.veloservice.ordenes.interfaces.rest.dto.ComentarioRequest;
import java.time.OffsetDateTime;
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

        OrdenComentario comentario = OrdenComentario.builder()
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .texto(texto)
                .createdAt(OffsetDateTime.now())
                .build();

        return toResult(comentarioRepository.save(comentario));
    }

    public ComentarioResult agregarComentario(UUID ordenId, ComentarioRequest request) {
        return agregar(ordenId, request.getTexto());
    }

    private ComentarioResult toResult(OrdenComentario c) {
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
