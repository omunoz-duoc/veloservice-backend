package com.veloservice.ordenes.interfaces.mapper;

import com.veloservice.ordenes.application.dto.MecanicoResult;
import com.veloservice.ordenes.interfaces.rest.MecanicoResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class MecanicoMapper {
    private MecanicoMapper() {
    }

    public static MecanicoResponse toResponse(MecanicoResult result) {
        return new MecanicoResponse(
                result.getId(),
                result.getNombre(),
                result.getApellido(),
                result.getIniciales(),
                result.getEmail(),
                result.getActivo(),
                result.getSucursalId()
        );
    }

    public static List<MecanicoResponse> toResponseList(List<MecanicoResult> results) {
        return results.stream()
                .map(MecanicoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
