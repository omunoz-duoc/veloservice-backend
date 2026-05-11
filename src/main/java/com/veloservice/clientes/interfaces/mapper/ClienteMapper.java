package com.veloservice.clientes.interfaces.mapper;

import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.application.dto.ClienteResult;
import com.veloservice.clientes.application.dto.MembresiaActualResult;
import com.veloservice.clientes.interfaces.rest.ClienteBusquedaResponse;
import com.veloservice.clientes.interfaces.rest.ClienteRequest;
import com.veloservice.clientes.interfaces.rest.ClienteResponse;
import com.veloservice.clientes.interfaces.rest.MembresiaActualResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class ClienteMapper {
    private ClienteMapper() {
    }

    public static ClienteCreateCommand toCommand(ClienteRequest request) {
        return new ClienteCreateCommand(
                request.getNombre(),
                request.getApellido(),
                request.getRut(),
                request.getTelefono(),
                request.getEmail(),
                request.getDireccion()
        );
    }

    public static ClienteResponse toResponse(ClienteResult result) {
        return ClienteResponse.builder()
                .id(result.getId())
                .nombre(result.getNombre())
                .apellido(result.getApellido())
                .rut(result.getRut())
                .telefono(result.getTelefono())
                .email(result.getEmail())
                .direccion(result.getDireccion())
                .membresiaActual(toMembresiaResponse(result.getMembresiaActual()))
                .build();
    }

    public static List<ClienteResponse> toResponseList(List<ClienteResult> results) {
        return results.stream()
                .map(ClienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static ClienteBusquedaResponse toBusquedaResponse(ClienteResult result) {
        return ClienteBusquedaResponse.builder()
                .id(result.getId())
                .nombreCompleto(buildNombreCompleto(result.getNombre(), result.getApellido()))
                .email(result.getEmail())
                .telefono(result.getTelefono())
                .rut(result.getRut())
                .build();
    }

    public static List<ClienteBusquedaResponse> toBusquedaResponseList(List<ClienteResult> results) {
        return results.stream()
                .map(ClienteMapper::toBusquedaResponse)
                .collect(Collectors.toList());
    }

    private static MembresiaActualResponse toMembresiaResponse(MembresiaActualResult result) {
        if (result == null) {
            return null;
        }
        return MembresiaActualResponse.builder()
                .nombre(result.getNombre())
                .descuento(result.getDescuento())
                .build();
    }

    private static String buildNombreCompleto(String nombre, String apellido) {
        String nombrePart = nombre == null ? "" : nombre.trim();
        String apellidoPart = apellido == null ? "" : apellido.trim();
        if (nombrePart.isEmpty()) {
            return apellidoPart;
        }
        if (apellidoPart.isEmpty()) {
            return nombrePart;
        }
        return nombrePart + " " + apellidoPart;
    }
}
