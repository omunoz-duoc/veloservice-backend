package com.veloservice.ordenes.interfaces.rest.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrdenDetalleResponse(
    UUID id,
    String numeroOrden,
    UUID tallerId,
    UUID sucursalId,
    CatalogoResponse estado,
    CatalogoResponse tipo,
    OffsetDateTime fechaIngreso,
    OffsetDateTime fechaPrometida,
    OffsetDateTime fechaEntrega,
    String diagnosticoInicial,
    String diagnosticoFinal,
    String observacionesCliente,
    BicicletaDetalleResponse bicicleta,
    ClienteDetalleResponse cliente,
    MecanicoDetalleResponse mecanico,
    String prioridad,
    List<ComentarioResponse> comentarios,
    List<MultimediaResponse> multimedia
) {
    public record CatalogoResponse(UUID id, String codigo, String nombre) {}

    public record BicicletaDetalleResponse(
        UUID id, String marca, String modelo, String tipo, String color, String numeroSerie
    ) {}

    public record ClienteDetalleResponse(
        UUID id, String nombre, String apellido, String telefono, String email, String rut
    ) {}

    public record MecanicoDetalleResponse(UUID id, String nombre, String apellido) {}

    public record ComentarioResponse(String usuario, String texto, OffsetDateTime createdAt) {}

    public record MultimediaResponse(
        String usuario, String tipoArchivo, String url, String etapa, String descripcion
    ) {}
}
