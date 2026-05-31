package com.veloservice.clientes.interfaces.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class HojaVidaBicicletaResponse {
    private UUID bicicletaId;
    private String marca;
    private String modelo;
    private String color;
    private String tipo;
    private String numeroSerie;
    private List<ServicioDTO> servicios;
    private List<RepuestoDTO> repuestos;
    private List<MultimediaDTO> multimedia;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ServicioDTO {
        private UUID ordenId;
        private String descripcion;
        private OffsetDateTime fechaIngreso;
        private OffsetDateTime fechaEntrega;
        private String estado;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class RepuestoDTO {
        private UUID productoId;
        private String nombre;
        private int cantidad;
        private String notas;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class MultimediaDTO {
        private UUID id;
        private String url;
        private String tipoArchivo;
        private String descripcion;
        private OffsetDateTime createdAt;
    }
}
