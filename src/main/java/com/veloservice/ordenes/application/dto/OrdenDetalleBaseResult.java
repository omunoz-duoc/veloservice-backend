package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenDetalleBaseResult(
        UUID id,
        String numeroOrden,
        UUID tallerId,
        UUID sucursalId,
        UUID estadoId,
        String estadoCodigo,
        String estadoNombre,
        UUID tipoId,
        String tipoCodigo,
        String tipoNombre,
        OffsetDateTime fechaIngreso,
        OffsetDateTime fechaPrometida,
        OffsetDateTime fechaEntrega,
        String diagnosticoInicial,
        String diagnosticoFinal,
        String observacionesCliente,
        UUID bicicletaId,
        String bicicletaMarca,
        String bicicletaModelo,
        String bicicletaTipo,
        String bicicletaAro,
        String bicicletaColor,
        String bicicletaNumeroSerie,
        Integer bicicletaAnio,
        String bicicletaFotoUrl,
        String bicicletaNotas,
        UUID clienteId,
        String clienteNombre,
        String clienteApellido,
        String clienteTelefono,
        String clienteEmail,
        String clienteRut,
        String clienteDireccion,
        UUID mecanicoId,
        String mecanicoNombre,
        String mecanicoApellido,
        String prioridad
) {
    public OrdenDetalleBaseResult(
            UUID id, String numeroOrden, UUID tallerId, UUID sucursalId,
            UUID estadoId, String estadoCodigo, String estadoNombre,
            UUID tipoId, String tipoCodigo, String tipoNombre,
            OffsetDateTime fechaIngreso, OffsetDateTime fechaPrometida, OffsetDateTime fechaEntrega,
            String diagnosticoInicial, String diagnosticoFinal, String observacionesCliente,
            UUID bicicletaId, String bicicletaMarca, String bicicletaModelo, String bicicletaTipo,
            String bicicletaAro, String bicicletaColor, String bicicletaNumeroSerie,
            Integer bicicletaAnio, String bicicletaFotoUrl, String bicicletaNotas,
            UUID clienteId, String clienteNombre, String clienteApellido,
            String clienteTelefono, String clienteEmail, String clienteRut,
            UUID mecanicoId, String mecanicoNombre, String mecanicoApellido, String prioridad
    ) {
        this(id, numeroOrden, tallerId, sucursalId, estadoId, estadoCodigo, estadoNombre,
                tipoId, tipoCodigo, tipoNombre, fechaIngreso, fechaPrometida, fechaEntrega,
                diagnosticoInicial, diagnosticoFinal, observacionesCliente, bicicletaId,
                bicicletaMarca, bicicletaModelo, bicicletaTipo, bicicletaAro, bicicletaColor,
                bicicletaNumeroSerie, bicicletaAnio, bicicletaFotoUrl, bicicletaNotas,
                clienteId, clienteNombre, clienteApellido, clienteTelefono, clienteEmail,
                clienteRut, null, mecanicoId, mecanicoNombre, mecanicoApellido, prioridad);
    }
}
