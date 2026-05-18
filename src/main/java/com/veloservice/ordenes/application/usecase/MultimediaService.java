package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.ordenes.application.dto.MultimediaCreateCommand;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.domain.model.Multimedia;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MultimediaService {

    private final MultimediaRepository multimediaRepository;

    @Transactional
    public MultimediaResult subir(UUID ordenId, String etapa, MultimediaCreateCommand command) {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) throw new IllegalStateException("Usuario no presente en contexto");

        EtapaMultimediaEnum etapaEnum = EtapaMultimediaEnum.valueOf(etapa.toLowerCase(Locale.ROOT));
        Multimedia m = Multimedia.builder()
                .ordenId(ordenId)
                .usuarioId(usuarioId)
                .url(command.getUrl())
                .tipoArchivo(command.getTipoArchivo())
            .etapa(etapaEnum)
                .descripcion(command.getDescripcion())
                .build();

        return toResult(multimediaRepository.save(m));
    }

    @Transactional(readOnly = true)
    public List<MultimediaResult> listarPorOrden(UUID ordenId) {
        return multimediaRepository.findByOrdenId(ordenId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    @Transactional
    public void eliminar(UUID id) {
        multimediaRepository.deleteById(id);
    }

    private MultimediaResult toResult(Multimedia m) {
        return MultimediaResult.builder()
                .id(m.getId())
                .ordenId(m.getOrdenId())
                .usuarioId(m.getUsuarioId())
                .url(m.getUrl())
                .tipoArchivo(m.getTipoArchivo())
                .etapa(m.getEtapa())
                .descripcion(m.getDescripcion())
                .createdAt(m.getCreatedAt())
                .build();
    }
}