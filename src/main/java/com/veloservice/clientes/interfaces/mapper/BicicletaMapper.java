package com.veloservice.clientes.interfaces.mapper;

import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.application.dto.BicicletaResult;
import com.veloservice.clientes.interfaces.rest.dto.BicicletaClienteResponse;
import com.veloservice.clientes.interfaces.rest.dto.BicicletaRequest;
import com.veloservice.clientes.interfaces.rest.dto.BicicletaResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class BicicletaMapper {
    private BicicletaMapper() {
    }

    public static BicicletaCreateCommand toCommand(BicicletaRequest request) {
        return new BicicletaCreateCommand(
                request.getMarca(),
                request.getModelo(),
                request.getTipo(),
                request.getAro(),
                request.getColor(),
                request.getNumeroSerie(),
                request.getAnio(),
                request.getNotas()
        );
    }

    public static BicicletaResponse toResponse(BicicletaResult result) {
        return BicicletaResponse.builder()
                .id(result.getId())
                .clienteId(result.getClienteId())
                .marca(result.getMarca())
                .modelo(result.getModelo())
                .tipo(result.getTipo())
                .aro(result.getAro())
                .color(result.getColor())
                .numeroSerie(result.getNumeroSerie())
                .anio(result.getAnio())
                .notas(result.getNotas())
                .build();
    }

    public static List<BicicletaResponse> toResponseList(List<BicicletaResult> results) {
        return results.stream()
                .map(BicicletaMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static BicicletaClienteResponse toClienteResponse(BicicletaResult result) {
        return BicicletaClienteResponse.builder()
                .id(result.getId())
                .marcaModelo(buildMarcaModelo(result.getMarca(), result.getModelo()))
                .tipo(result.getTipo())
                .talla(result.getAro())
                .color(result.getColor())
                .numeroSerie(result.getNumeroSerie())
                .build();
    }

    public static List<BicicletaClienteResponse> toClienteResponseList(List<BicicletaResult> results) {
        return results.stream()
                .map(BicicletaMapper::toClienteResponse)
                .collect(Collectors.toList());
    }

    private static String buildMarcaModelo(String marca, String modelo) {
        String marcaPart = marca == null ? "" : marca.trim();
        String modeloPart = modelo == null ? "" : modelo.trim();
        if (marcaPart.isEmpty()) {
            return modeloPart;
        }
        if (modeloPart.isEmpty()) {
            return marcaPart;
        }
        return marcaPart + " " + modeloPart;
    }
}
