package com.veloservice.ordenes.interfaces.mapper;

import com.veloservice.administracion.application.dto.MecanicoDisponibleResult;
import com.veloservice.administracion.application.dto.MecanicoResult;
import com.veloservice.administracion.interfaces.rest.MecanicoDisponibleResponse;
import com.veloservice.administracion.interfaces.rest.MecanicoResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class MecanicoMapper {
    private MecanicoMapper() {
    }

    public static MecanicoResponse toResponse(MecanicoResult result) {
        return MecanicoResponse.builder()
                .nombre(result.getNombre())
                .apellido(result.getApellido())
                .ordenesEnCurso(List.of())
                .build();
    }

    public static List<MecanicoResponse> toResponseList(List<MecanicoResult> results) {
        return results.stream()
                .map(MecanicoMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static MecanicoDisponibleResponse toDisponibleResponse(MecanicoDisponibleResult result) {
        return new MecanicoDisponibleResponse(
                result.getId(),
                result.getNombre(),
                result.getApellido(),
                result.getIniciales()
        );
    }

    public static List<MecanicoDisponibleResponse> toDisponibleResponseList(List<MecanicoDisponibleResult> results) {
        return results.stream()
                .map(MecanicoMapper::toDisponibleResponse)
                .collect(Collectors.toList());
    }
}
