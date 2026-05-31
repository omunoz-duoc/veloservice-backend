package com.veloservice.ordenes.interfaces.mapper;

import com.veloservice.ordenes.application.dto.MultimediaCreateCommand;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.interfaces.rest.dto.MultimediaRequest;
import com.veloservice.ordenes.interfaces.rest.dto.MultimediaResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class MultimediaMapper {
    private MultimediaMapper() {
    }

    public static MultimediaCreateCommand toCommand(MultimediaRequest request) {
        return new MultimediaCreateCommand(
                request.getUrl(),
                request.getTipoArchivo(),
                request.getDescripcion()
        );
    }

    public static MultimediaResponse toResponse(MultimediaResult result) {
        return MultimediaResponse.builder()
                .id(result.getId())
                .ordenId(result.getOrdenId())
                .usuarioId(result.getUsuarioId())
                .url(result.getUrl())
                .tipoArchivo(result.getTipoArchivo())
                .etapa(result.getEtapa())
                .descripcion(result.getDescripcion())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public static List<MultimediaResponse> toResponseList(List<MultimediaResult> results) {
        return results.stream()
                .map(MultimediaMapper::toResponse)
                .collect(Collectors.toList());
    }
}
