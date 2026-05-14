package com.veloservice.clientes.interfaces.mapper;

import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.application.dto.ClienteResult;
import com.veloservice.clientes.application.dto.MembresiaActualResult;
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
                .id(result.getId() != null
                        ? "CL-" + result.getId().toString().substring(0, 8).toUpperCase()
                        : null)
                .nombre(result.getNombre())
                .apellido(result.getApellido())
                .tipo(result.getTipo())
                .rut(result.getRut())
                .email(result.getEmail())
                .telefono(result.getTelefono())
                .bicicletasCount(result.getBicicletasCount())
                .ordenesCount(result.getOrdenesCount())
                .totalGastado(result.getTotalGastado())
                .membresiaActual(toMembresiaResponse(result.getMembresiaActual()))
                .build();
    }

    public static List<ClienteResponse> toResponseList(List<ClienteResult> results) {
        return results.stream()
                .map(ClienteMapper::toResponse)
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
}