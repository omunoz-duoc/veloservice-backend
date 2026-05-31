package com.veloservice.finanzas.interfaces.mapper;

import com.veloservice.finanzas.application.dto.CobroCreateCommand;
import com.veloservice.finanzas.application.dto.CobroResult;
import com.veloservice.finanzas.application.dto.FinanzasHoyResult;
import com.veloservice.finanzas.interfaces.rest.dto.CobroRequest;
import com.veloservice.finanzas.interfaces.rest.dto.CobroResponse;
import com.veloservice.finanzas.interfaces.rest.dto.FinanzasHoyResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class CobroMapper {
    private CobroMapper() {
    }

    public static CobroCreateCommand toCommand(CobroRequest request) {
        return new CobroCreateCommand(
                request.getOrdenId(),
                request.getTipoDocumento(),
                request.getNumeroDocumento(),
                request.getMetodoPago(),
                request.getDescuentoManual()
        );
    }

    public static CobroResponse toResponse(CobroResult result) {
        return CobroResponse.builder()
                .id(result.getId())
                .ordenId(result.getOrdenId())
                .usuarioId(result.getUsuarioId())
                .tipoDocumento(result.getTipoDocumento())
                .numeroDocumento(result.getNumeroDocumento())
                .subtotalServicios(result.getSubtotalServicios())
                .subtotalProductos(result.getSubtotalProductos())
                .descuentoMembresia(result.getDescuentoMembresia())
                .descuentoManual(result.getDescuentoManual())
                .neto(result.getNeto())
                .iva(result.getIva())
                .total(result.getTotal())
                .metodoPago(result.getMetodoPago())
                .estado(result.getEstado())
                .folioSii(result.getFolioSii())
                .estadoSii(result.getEstadoSii())
                .fechaPago(result.getFechaPago())
                .anuladaAt(result.getAnuladaAt())
                .motivoAnulacion(result.getMotivoAnulacion())
                .createdAt(result.getCreatedAt())
                .build();
    }

    public static List<CobroResponse> toResponseList(List<CobroResult> results) {
        return results.stream()
                .map(CobroMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static FinanzasHoyResponse toResponse(FinanzasHoyResult result) {
        return new FinanzasHoyResponse(result.totalIngresosHoy(), result.deltaVsAyerPorcentaje());
    }
}
