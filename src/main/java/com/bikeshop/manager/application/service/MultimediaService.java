package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.MultimediaRequest;
import com.bikeshop.manager.domain.tenant.Multimedia;
import com.bikeshop.manager.infrastructure.persistence.repository.MultimediaRepository;
import com.bikeshop.manager.infrastructure.security.UsuarioContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MultimediaService {

    private final MultimediaRepository multimediaRepository;

    @Transactional
    public Multimedia subir(UUID ordenId, String etapa, MultimediaRequest request) {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) throw new IllegalStateException("Usuario no presente en contexto");

        Multimedia m = Multimedia.builder()
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .url(request.getUrl())
                .tipoArchivo(request.getTipoArchivo())
                .etapa(etapa)
                .descripcion(request.getDescripcion())
                .build();

        return multimediaRepository.save(m);
    }

    @Transactional(readOnly = true)
    public List<Multimedia> listarPorOrden(UUID ordenId) {
        return multimediaRepository.findByOrdenId(ordenId);
    }
}
