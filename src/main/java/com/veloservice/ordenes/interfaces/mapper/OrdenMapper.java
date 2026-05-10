package com.veloservice.ordenes.interfaces.mapper;

import com.veloservice.ordenes.application.dto.MultimediaCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenListaEntregaResult;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.interfaces.rest.EstadoChangeRequest;
import com.veloservice.ordenes.interfaces.rest.MultimediaRequest;
import com.veloservice.ordenes.interfaces.rest.OrdenListaEntregaResponse;
import com.veloservice.ordenes.interfaces.rest.OrdenProductoRequest;
import com.veloservice.ordenes.interfaces.rest.OrdenRequest;
import com.veloservice.ordenes.interfaces.rest.OrdenResponse;
import com.veloservice.ordenes.interfaces.rest.OrdenServicioRequest;

import java.util.List;
import java.util.stream.Collectors;

public final class OrdenMapper {
    private OrdenMapper() {
    }

    public static OrdenCreateCommand toCommand(OrdenRequest request) {
        List<MultimediaCreateCommand> multimedia = null;
        if (request.getMultimedia() != null) {
            multimedia = request.getMultimedia().stream()
                    .map(OrdenMapper::toMultimediaCommand)
                    .collect(Collectors.toList());
        }
        return new OrdenCreateCommand(
                request.getBicicletaId(),
                request.getTipo(),
                request.getDiagnosticoInicial(),
                request.getObservacionesCliente(),
                multimedia
        );
    }

    public static OrdenResponse toResponse(OrdenResult result) {
        return OrdenResponse.builder()
                .id(result.getId())
                .numeroOrden(result.getNumeroOrden())
                .estado(result.getEstado())
                .tipo(result.getTipo())
                .bicicletaId(result.getBicicletaId())
                .mecanicoId(result.getMecanicoId())
                .diagnosticoInicial(result.getDiagnosticoInicial())
                .fechaIngreso(result.getFechaIngreso())
                .fechaPrometida(result.getFechaPrometida())
                .build();
    }

    public static List<OrdenResponse> toResponseList(List<OrdenResult> results) {
        return results.stream()
                .map(OrdenMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static OrdenListaEntregaResponse toListaEntregaResponse(OrdenListaEntregaResult result) {
        return new OrdenListaEntregaResponse(
                result.getId(),
                result.getNumeroOrden(),
                result.getClienteNombre(),
                result.getMecanicoAsignado(),
                result.getFechaIngreso(),
                result.getEstado(),
                result.getTotalEstimado()
        );
    }

    public static List<OrdenListaEntregaResponse> toListaEntregaResponseList(List<OrdenListaEntregaResult> results) {
        return results.stream()
                .map(OrdenMapper::toListaEntregaResponse)
                .collect(Collectors.toList());
    }

    public static OrdenEstadoChangeCommand toEstadoChangeCommand(EstadoChangeRequest request) {
        return new OrdenEstadoChangeCommand(
                request.getNuevoEstado(),
                request.getObservacion()
        );
    }

    public static OrdenServicioAddCommand toServicioCommand(OrdenServicioRequest request) {
        return new OrdenServicioAddCommand(request.getServicioId(), request.getNotas());
    }

    public static OrdenProductoAddCommand toProductoCommand(OrdenProductoRequest request) {
        return new OrdenProductoAddCommand(
                request.getProductoId(),
                request.getCantidad(),
                request.getProporcionadoPorCliente(),
                request.getNotas()
        );
    }

    private static MultimediaCreateCommand toMultimediaCommand(MultimediaRequest request) {
        return new MultimediaCreateCommand(
                request.getUrl(),
                request.getTipoArchivo(),
                request.getDescripcion()
        );
    }
}
