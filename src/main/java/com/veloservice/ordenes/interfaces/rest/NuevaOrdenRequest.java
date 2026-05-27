package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.domain.PrioridadOrdenEnum;
import com.veloservice.ordenes.domain.TipoOrdenEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Work order creation request payload for the modal flow.
 */
@Data
public class NuevaOrdenRequest {
    private UUID clienteId;
    private UUID bicicletaId;
    @Valid
    private ClienteNuevoRequest clienteNuevo;
    @Valid
    private BicicletaNuevaRequest bicicletaNueva;
    @NotNull
    private TipoOrdenEnum tipoTrabajo;
    @NotNull
    private PrioridadOrdenEnum prioridad;
    @NotNull
    private LocalDate fechaEstimadaEntrega;
    private UUID mecanicoAsignadoId;
    @NotBlank
    private String descripcionTrabajo;
    private String notasInternas;

    @Data
    public static class ClienteNuevoRequest {
        @NotBlank
        private String nombreCompleto;
        private String email;
        private String telefono;
        private String rut;
    }

    @Data
    public static class BicicletaNuevaRequest {
        @NotBlank
        private String marcaModelo;
        @NotBlank
        private String tipo;
        @NotBlank
        private String talla;
        @NotBlank
        private String color;
        private String numeroSerie;
    }
}
