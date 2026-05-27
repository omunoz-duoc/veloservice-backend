package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.ordenes.domain.TipoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
public class OrdenResponse {
    private String id;
    private EstadoOrdenEnum estado;
    private TipoOrdenEnum tipo;
    private String descripcion;
    private String mecanico;
    private ClienteResponse cliente;
    private BicicletaResponse bicicleta;
    @JsonProperty("fecha_ingreso")
    private OffsetDateTime fechaIngreso;
    @JsonProperty("fecha_estimada")
    private OffsetDateTime fechaEstimada;

    @Data
    @Builder
    @AllArgsConstructor
    public static class ClienteResponse {
        private String nombre;
        private String apellido;
        private String telefono;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class BicicletaResponse {
        private String marca;
        private String modelo;
        private String tipo;
        private String color;
        private String talla;
    }
}