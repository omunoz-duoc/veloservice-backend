package com.veloservice.ordenes.application.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrdenDetalleResult(
    UUID id,
    String numeroOrden,
    UUID tallerId,
    UUID sucursalId,
    UUID estadoId, String estadoCodigo,String estadoNombre,
    UUID tipoId, String tipoCodigo, String tipoNombre,
    OffsetDateTime fechaIngreso,
    OffsetDateTime fechaPrometida,
    OffsetDateTime fechaEntrega,
    String diagnosticoInicial,
    String diagnosticoFinal,
    String observacionesCliente,
    UUID bicicletaId, String bicicletaMarca, String bicicletaModelo,String bicicletaTipo, String bicicletaColor, String bicicletaNumeroSerie,String bicicletaAro, Integer bicicletaAnio, String bicicletaFotoUrl, String bicicletaNotas,
    UUID clienteId, String clienteNombre, String clienteApellido,
    String clienteTelefono, String clienteEmail, String clienteRut,
    UUID mecanicoId, String mecanicoNombre, String mecanicoApellido,
    String prioridad,
    List<ComentarioResult> comentarios,
    List<MultimediaResult> multimedia,
    List<OrdenProductoResult> productos,
    List<OrdenServicioResult> servicios
) {}
