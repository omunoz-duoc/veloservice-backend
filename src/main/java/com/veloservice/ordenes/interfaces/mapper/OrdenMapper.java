package com.veloservice.ordenes.interfaces.mapper;

import com.veloservice.ordenes.application.dto.NuevaOrdenCommand;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenListaEntregaResult;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenResumenResult;
import com.veloservice.ordenes.application.dto.OrdenResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.interfaces.rest.EstadoChangeRequest;
import com.veloservice.ordenes.interfaces.rest.MultimediaRequest;
import com.veloservice.ordenes.interfaces.rest.OrdenListaEntregaResponse;
import com.veloservice.ordenes.interfaces.rest.OrdenResumenResponse;
import com.veloservice.ordenes.interfaces.rest.NuevaOrdenRequest;
import com.veloservice.ordenes.interfaces.rest.OrdenProductoRequest;
import com.veloservice.ordenes.interfaces.rest.OrdenResponse;
import com.veloservice.ordenes.interfaces.rest.OrdenServicioRequest;

import java.util.List;
import java.util.stream.Collectors;

public final class OrdenMapper {
    private OrdenMapper() {
    }

    public static NuevaOrdenCommand toCommand(NuevaOrdenRequest request) {
        NuevaOrdenCommand.ClienteNuevoCommand clienteNuevo = null;
        if (request.getClienteNuevo() != null) {
            clienteNuevo = new NuevaOrdenCommand.ClienteNuevoCommand(
                    request.getClienteNuevo().getNombreCompleto(),
                    request.getClienteNuevo().getEmail(),
                    request.getClienteNuevo().getTelefono(),
                    request.getClienteNuevo().getRut()
            );
        }
        NuevaOrdenCommand.BicicletaNuevaCommand bicicletaNueva = null;
        if (request.getBicicletaNueva() != null) {
            bicicletaNueva = new NuevaOrdenCommand.BicicletaNuevaCommand(
                    request.getBicicletaNueva().getMarcaModelo(),
                    request.getBicicletaNueva().getTipo(),
                    request.getBicicletaNueva().getTalla(),
                    request.getBicicletaNueva().getColor(),
                    request.getBicicletaNueva().getNumeroSerie()
            );
        }
        return new NuevaOrdenCommand(
                request.getClienteId(),
                request.getBicicletaId(),
                clienteNuevo,
                bicicletaNueva,
                request.getTipoTrabajo(),
                request.getPrioridad(),
                request.getFechaEstimadaEntrega(),
                request.getMecanicoAsignadoId(),
                request.getDescripcionTrabajo(),
                request.getNotasInternas()
        );
    }

    public static OrdenResponse toResponse(OrdenResult result) {
        return OrdenResponse.builder()
                .id(result.getId())
                .numeroOrden(result.getNumeroOrden())
                .estado(result.getEstado())
                .tipo(result.getTipo())
                .prioridad(result.getPrioridad())
                .bicicletaId(result.getBicicletaId())
                .mecanicoId(result.getMecanicoId())
                .mecanicoAsignadoId(result.getMecanicoAsignadoId())
                .descripcionTrabajo(result.getDescripcionTrabajo())
                .notasInternas(result.getNotasInternas())
                .diagnosticoInicial(result.getDiagnosticoInicial())
                .fechaIngreso(result.getFechaIngreso())
                .fechaPrometida(result.getFechaPrometida())
                .fechaEstimadaEntrega(result.getFechaEstimadaEntrega())
                .build();
    }

    public static List<OrdenResponse> toResponseList(List<OrdenResult> results) {
        return results.stream()
                .map(OrdenMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static OrdenResumenResponse toResumenResponse(OrdenResumenResult result) {
        return new OrdenResumenResponse(
                result.getExternalId(),
                result.getTipo(),
                result.getFechaIngreso(),
                result.getNombreMecanico(),
                result.getNombreCliente(),
                result.getDescripcion(),
                result.getObservacionesCliente(),
                new OrdenResumenResponse.BicicletaResumenResponse(
                        result.getBicicletaMarca(),
                        result.getBicicletaColor(),
                        result.getBicicletaTipo(),
                        result.getBicicletaTalla()
                ),
                result.getEstado()
        );
    }

    public static List<OrdenResumenResponse> toResumenResponseList(List<OrdenResumenResult> results) {
        return results.stream()
                .map(OrdenMapper::toResumenResponse)
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

}
