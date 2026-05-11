package com.veloservice.administracion.interfaces.mapper;

import com.veloservice.administracion.application.dto.MecanicoDisponibleResult;
import com.veloservice.administracion.interfaces.rest.MecanicoDisponibleResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class MecanicoMapper {
    private MecanicoMapper() {
    }

    public static MecanicoDisponibleResponse toResponse(MecanicoDisponibleResult result) {
        return MecanicoDisponibleResponse.builder()
                .id(result.getId())
                .nombre(result.getNombre())
                .apellido(result.getApellido())
                .iniciales(result.getIniciales())
                .build();
    }

    public static List<MecanicoDisponibleResponse> toResponseList(List<MecanicoDisponibleResult> results) {
        return results.stream()
                .map(MecanicoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
