package com.veloservice.administracion.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class MecanicoPerfilResponse {
    private UUID mecanicoId;
    private String nombre;
    private String apellido;
    private int totalOrdenes;
    private int ordenesCompletadas;
    private double tiempoPromedioDias;
    private List<OrdenResumenDTO> ordenesRecientes;
    private String nivelTecnico;

    @Data
    @Builder
    @AllArgsConstructor
    public static class OrdenResumenDTO {
        private UUID id;
        private String descripcion;
        private String estado;
        private String cliente;
    }
}
