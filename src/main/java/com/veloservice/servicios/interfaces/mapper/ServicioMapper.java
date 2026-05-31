package com.veloservice.servicios.interfaces.mapper;

import com.veloservice.servicios.application.dto.ServicioCreateCommand;
import com.veloservice.servicios.application.dto.ServicioResult;
import com.veloservice.servicios.application.dto.SucursalServicioPrecioCommand;
import com.veloservice.servicios.application.dto.SucursalServicioResult;
import com.veloservice.servicios.interfaces.rest.dto.ServicioRequest;
import com.veloservice.servicios.interfaces.rest.dto.ServicioResponse;
import com.veloservice.servicios.interfaces.rest.dto.SucursalServicioRequest;
import com.veloservice.servicios.interfaces.rest.dto.SucursalServicioResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class ServicioMapper {
    private ServicioMapper() {
    }

    public static ServicioCreateCommand toCommand(ServicioRequest request) {
        return new ServicioCreateCommand(
                request.getNombre(),
                request.getDescripcion(),
                request.getPrecioBase(),
                request.getActivo()
        );
    }

    public static ServicioResponse toResponse(ServicioResult result) {
        return ServicioResponse.builder()
                .id(result.getId())
                .nombre(result.getNombre())
                .descripcion(result.getDescripcion())
                .precioBase(result.getPrecioBase())
                .activo(result.getActivo())
                .build();
    }

    public static List<ServicioResponse> toResponseList(List<ServicioResult> results) {
        return results.stream()
                .map(ServicioMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static SucursalServicioPrecioCommand toSucursalCommand(SucursalServicioRequest request) {
        return new SucursalServicioPrecioCommand(
                request.getServicioId(),
                request.getPrecioPersonalizado()
        );
    }

    public static SucursalServicioResponse toSucursalResponse(SucursalServicioResult result) {
        return SucursalServicioResponse.builder()
                .id(result.getId())
                .sucursalId(result.getSucursalId())
                .servicioId(result.getServicioId())
                .precioPersonalizado(result.getPrecioPersonalizado())
                .activo(result.getActivo())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public static List<SucursalServicioResponse> toSucursalResponseList(List<SucursalServicioResult> results) {
        return results.stream()
                .map(ServicioMapper::toSucursalResponse)
                .collect(Collectors.toList());
    }
}
