package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.enums.EtapaMultimediaEnum;
import com.veloservice.config.enums.TipoArchivoEnum;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.ordenes.application.dto.MultimediaCreateCommand;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.PresignResult;
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
    private final StorageService storageService;

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

    public PresignResult generarPresign(UUID ordenId, String fileName, String contentType, long fileSize) {
        if (!List.of("image/jpeg", "image/png").contains(contentType)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Use image/jpeg o image/png");
        }
        if (fileSize > 10_485_760L) {
            throw new IllegalArgumentException("El archivo supera el límite de 10 MB");
        }
        String ext = "image/jpeg".equals(contentType) ? "jpg" : "png";
        String fileKey = "ordenes/" + ordenId + "/" + UUID.randomUUID() + "." + ext;
        String uploadUrl = storageService.presign(fileKey, contentType, 10);
        return new PresignResult(uploadUrl, fileKey);
    }

    public MultimediaResult confirmar(UUID ordenId, String etapa, String fileKey,
                                      TipoArchivoEnum tipoArchivo, String descripcion) {
        String publicUrl = storageService.publicUrl(fileKey);
        return subir(ordenId, etapa, new MultimediaCreateCommand(publicUrl, tipoArchivo, descripcion));
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