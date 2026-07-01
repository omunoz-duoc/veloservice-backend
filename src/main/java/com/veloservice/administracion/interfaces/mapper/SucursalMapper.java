package com.veloservice.administracion.interfaces.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.veloservice.administracion.application.dto.SucursalResult;
import com.veloservice.administracion.interfaces.rest.SucursalRespone;

public class SucursalMapper {
    private SucursalMapper() {
    }

    public static SucursalRespone toResponse(SucursalResult result) {
        return SucursalRespone.builder()
                .id(result.getId().toString())
                .nombre(result.getNombre())
                .build();
    }

    public static List<SucursalRespone> toResponseList(List<SucursalResult> results) {
        return results.stream().map(SucursalMapper::toResponse).collect(Collectors.toList());
    }
}
