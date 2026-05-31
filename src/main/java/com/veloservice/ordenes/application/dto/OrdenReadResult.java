package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrdenReadResult(
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
        UUID clienteId,
        String clienteNombre,
        String clienteApellido,
        String clienteTelefono,
        String clienteEmail,
        String clienteRut,
        UUID mecanicoId,
        String mecanicoNombre,
        String mecanicoApellido
) {
}
