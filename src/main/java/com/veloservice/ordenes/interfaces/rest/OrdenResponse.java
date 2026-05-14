package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.TipoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class OrdenResponse {
    private UUID id;
    private String numeroOrden;
    private EstadoOrdenEnum estado;
    private TipoOrdenEnum tipo;
    private String descripcion;
    private String mecanico;
    private ClienteResponse cliente;
    private BicicletaResponse bicicleta;
    private OffsetDateTime fechaIngreso;
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