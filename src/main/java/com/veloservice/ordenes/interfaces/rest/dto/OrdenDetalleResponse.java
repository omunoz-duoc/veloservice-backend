package com.veloservice.ordenes.interfaces.rest.dto;

import java.math.BigDecimal;
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
    String servicioResumen,
    BigDecimal montoTotal,
    List<ComentarioResponse> comentarios,
    List<MultimediaResponse> multimedia,
    List<ProductoResponse> productos,
    List<ServicioResponse> servicios,
    List<EstadoHistoryResponse> historialEstados
) {
    public record CatalogoResponse(UUID id, String codigo, String nombre) {}

    public record BicicletaDetalleResponse(
        UUID id, String marca, String modelo, String tipo, String aro, String color, String numeroSerie, Integer anio, String foto_url, String notas
    ) {}

    public record ClienteDetalleResponse(
        UUID id, String nombre, String apellido, String telefono, String email, String rut, String direccion
    ) {}

    public record MecanicoDetalleResponse(UUID id, String nombre, String apellido) {}

    public record ComentarioResponse(
        UUID id, UUID usuarioId, String usuario, String texto, OffsetDateTime createdAt
    ) {}

    public record MultimediaResponse(
        UUID id, UUID usuarioId, String usuario, String tipoArchivo, String categoria,
        String url, String etapa, String descripcion, OffsetDateTime createdAt
    ) {}

    public record ProductoResponse(
        UUID id,
        UUID productoId,
        String nombre,
        String sku,
        Integer cantidad,
        BigDecimal precioVenta,
        BigDecimal precioAplicado,
        String notas,
        Boolean proporcionadoPorCliente,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
    ) {}

    public record ServicioResponse(
        UUID id,
        UUID servicioId,
        String nombre,
        BigDecimal precioBase,
        BigDecimal precioAplicado,
        BigDecimal descuentoAplicado,
        String notas,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
    ) {}

    public record EstadoHistoryResponse(
        UUID id,
        CatalogoResponse estadoAnterior,
        CatalogoResponse estadoNuevo,
        String observacion,
        UUID usuarioId,
        String usuario,
        OffsetDateTime createdAt
    ) {}
}
