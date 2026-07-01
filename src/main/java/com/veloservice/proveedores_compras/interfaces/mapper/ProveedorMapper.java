package com.veloservice.proveedores_compras.interfaces.mapper;

import com.veloservice.proveedores_compras.application.dto.ProveedorCreateCommand;
import com.veloservice.proveedores_compras.application.dto.ProveedorResult;
import com.veloservice.proveedores_compras.application.dto.SucursalProveedorCreateCommand;
import com.veloservice.proveedores_compras.application.dto.SucursalProveedorResult;
import com.veloservice.proveedores_compras.interfaces.rest.dto.ProveedorRequest;
import com.veloservice.proveedores_compras.interfaces.rest.dto.ProveedorResponse;
import com.veloservice.proveedores_compras.interfaces.rest.dto.ProveedorSucursalRequest;
import com.veloservice.proveedores_compras.interfaces.rest.dto.SucursalProveedorResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class ProveedorMapper {
    private ProveedorMapper() {
    }

    public static ProveedorCreateCommand toCommand(ProveedorRequest request) {
        return new ProveedorCreateCommand(
                request.getNombre(),
                request.getRut(),
                request.getTelefono(),
                request.getEmail(),
                request.getDireccion(),
                request.getCondicionPago(),
                request.getContactoAsignado()
        );
    }

    public static SucursalProveedorCreateCommand toSucursalCommand(ProveedorSucursalRequest request) {
        return new SucursalProveedorCreateCommand(
                request.getProveedorId(),
                request.getCodigoCliente(),
                request.getCondicionPago(),
                request.getContactoAsignado()
        );
    }

    public static ProveedorResponse toResponse(ProveedorResult result) {
        return ProveedorResponse.builder()
                .id(result.getId())
                .nombre(result.getNombre())
                .rut(result.getRut())
                .telefono(result.getTelefono())
                .email(result.getEmail())
                .direccion(result.getDireccion())
                .activo(result.getActivo())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public static List<ProveedorResponse> toResponseList(List<ProveedorResult> results) {
        return results.stream()
                .map(ProveedorMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static SucursalProveedorResponse toResponse(SucursalProveedorResult result) {
        return SucursalProveedorResponse.builder()
                .id(result.getId())
                .sucursalId(result.getSucursalId())
                .proveedorId(result.getProveedorId())
                .codigoCliente(result.getCodigoCliente())
                .condicionPago(result.getCondicionPago())
                .contactoAsignado(result.getContactoAsignado())
                .activo(result.getActivo())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
