package com.veloservice.taller.internal.service;

import com.veloservice.taller.api.MultimediaRequest;
import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.taller.internal.entity.Multimedia;
import com.veloservice.taller.internal.repository.MultimediaRepository;
import com.veloservice.config.security.UsuarioContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MultimediaService {

    private final MultimediaRepository multimediaRepository;

    @Transactional
    public Multimedia subir(UUID ordenId, String etapa, MultimediaRequest request) {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) throw new IllegalStateException("Usuario no presente en contexto");

        EtapaMultimediaEnum etapaEnum = EtapaMultimediaEnum.valueOf(etapa.toLowerCase(Locale.ROOT));
        Multimedia m = Multimedia.builder()
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .url(request.getUrl())
                .tipoArchivo(request.getTipoArchivo())
            .etapa(etapaEnum)
                .descripcion(request.getDescripcion())
                .build();

        return multimediaRepository.save(m);
    }

    @Transactional(readOnly = true)
    public List<Multimedia> listarPorOrden(UUID ordenId) {
        return multimediaRepository.findByOrdenId(ordenId);
    }
}