package com.veloservice.ordenes.interfaces.mapper;

import com.veloservice.ordenes.application.dto.GarantiaCreateCommand;
import com.veloservice.ordenes.application.dto.GarantiaResult;
import com.veloservice.ordenes.interfaces.rest.dto.GarantiaRequest;
import com.veloservice.ordenes.interfaces.rest.dto.GarantiaResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class GarantiaMapper {
    private GarantiaMapper() {
    }

    public static GarantiaCreateCommand toCommand(GarantiaRequest request) {
        return new GarantiaCreateCommand(
                request.getOrdenProductoId(),
                request.getOrdenId(),
                request.getNumeroGarantia(),
                request.getDescripcionFalla(),
                request.getFechaInicio(),
                request.getFechaVencimiento(),
                request.getCondiciones(),
                request.getResolucion()
        );
    }

    public static GarantiaResponse toResponse(GarantiaResult result) {
        return GarantiaResponse.builder()
                .id(result.getId())
                .ordenId(result.getOrdenId())
                .numeroGarantia(result.getNumeroGarantia())
                .marcaBicicleta(result.getMarcaBicicleta())
                .componenteAfectado(result.getComponenteAfectado())
                .descripcionFalla(result.getDescripcionFalla())
                .estado(result.getEstado())
                .fechaInicio(result.getFechaInicio())
                .fechaVencimiento(result.getFechaVencimiento())
                .condiciones(result.getCondiciones())
                .resolucion(result.getResolucion())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .build();
    }

    public static List<GarantiaResponse> toResponseList(List<GarantiaResult> results) {
        return results.stream()
                .map(GarantiaMapper::toResponse)
                .collect(Collectors.toList());
    }
}
