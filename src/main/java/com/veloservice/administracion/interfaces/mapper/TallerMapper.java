package com.veloservice.administracion.interfaces.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.veloservice.administracion.application.dto.TallerResult;
import com.veloservice.administracion.interfaces.rest.dto.TallerResponse;

public class TallerMapper {
    private TallerMapper() {
    }

    public static TallerResponse toResponse(TallerResult result) {
        return TallerResponse.builder()
                .id(result.getId().toString())
                .rut(result.getRut())
                .nombre(result.getNombre())
                .telefono(result.getTelefono())
                .email(result.getEmail())
                .logoUrl(result.getLogoUrl())
                .planId(result.getPlanId() != null ? result.getPlanId().toString() : null)
                .activo(result.isActivo())
                .build();
    }

    public static List<TallerResponse> toResponseList(List<TallerResult> results) {
        return results.stream().map(TallerMapper::toResponse).collect(Collectors.toList());
    }
}
