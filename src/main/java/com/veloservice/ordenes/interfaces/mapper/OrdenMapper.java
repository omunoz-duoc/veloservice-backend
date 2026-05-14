package com.veloservice.ordenes.interfaces.mapper;

import com.veloservice.ordenes.application.dto.MultimediaCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.interfaces.rest.EstadoChangeRequest;
import com.veloservice.ordenes.interfaces.rest.MultimediaRequest;
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
        OrdenResponse.ClienteResponse cliente = null;
        if (result.getClienteNombre() != null) {
            cliente = OrdenResponse.ClienteResponse.builder()
                    .nombre(result.getClienteNombre())
                    .apellido(result.getClienteApellido())
                    .telefono(result.getClienteTelefono())
                    .build();
        }

        OrdenResponse.BicicletaResponse bicicleta = null;
        if (result.getBicicletaMarca() != null) {
            bicicleta = OrdenResponse.BicicletaResponse.builder()
                    .marca(result.getBicicletaMarca())
                    .modelo(result.getBicicletaModelo())
                    .tipo(result.getBicicletaTipo())
                    .color(result.getBicicletaColor())
                    .talla(result.getBicicletaTalla())
                    .build();
        }

        String mecanico = null;
        if (result.getMecanicoNombre() != null) {
            mecanico = result.getMecanicoNombre() + " " + result.getMecanicoApellido();
        }

        return OrdenResponse.builder()
                .id(result.getNumeroOrden())  // ← OT-123
                .estado(result.getEstado())
                .tipo(result.getTipo())
                .descripcion(result.getDiagnosticoInicial())
                .mecanico(mecanico)
                .cliente(cliente)
                .bicicleta(bicicleta)
                .fechaIngreso(result.getFechaIngreso())
                .fechaEstimada(result.getFechaPrometida())
                .build();
    }

    public static List<OrdenResponse> toResponseList(List<OrdenResult> results) {
        return results.stream()
                .map(OrdenMapper::toResponse)
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