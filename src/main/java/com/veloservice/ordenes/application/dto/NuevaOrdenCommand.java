package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.PrioridadOrdenEnum;
import com.veloservice.config.enums.TipoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Application command for creating a work order from the modal flow.
 */
@Data
@AllArgsConstructor
public class NuevaOrdenCommand {
    private UUID clienteId;
    private UUID bicicletaId;
    private ClienteNuevoCommand clienteNuevo;
    private BicicletaNuevaCommand bicicletaNueva;
    private TipoOrdenEnum tipoTrabajo;
    private PrioridadOrdenEnum prioridad;
    private LocalDate fechaEstimadaEntrega;
    private UUID mecanicoAsignadoId;
    private String descripcionTrabajo;
    private String notasInternas;

    @Data
    @AllArgsConstructor
    public static class ClienteNuevoCommand {
        private String nombreCompleto;
        private String email;
        private String telefono;
        private String rut;
    }

    @Data
    @AllArgsConstructor
    public static class BicicletaNuevaCommand {
        private String marcaModelo;
        private String tipo;
        private String talla;
        private String color;
        private String numeroSerie;
    }
}
