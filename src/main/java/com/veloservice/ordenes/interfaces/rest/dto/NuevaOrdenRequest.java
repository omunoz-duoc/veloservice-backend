package com.veloservice.ordenes.interfaces.rest.dto;

import com.veloservice.ordenes.domain.PrioridadOrdenEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class NuevaOrdenRequest {
    private UUID clienteId;
    @Valid
    private ClienteNuevoRequest clienteNuevo;
    private UUID bicicletaId;
    @Valid
    private BicicletaNuevaRequest bicicletaNueva;
    private UUID sucursalId;

    @NotBlank
    private String tipoTrabajo;
    @NotNull
    private PrioridadOrdenEnum prioridad;
    private UUID mecanicoId;
    private LocalDate fechaPrometida;
    private String diagnosticoInicial;
    private String observacionesCliente;

    private List<ServicioItem> servicios;
    private List<ProductoItem> productos;

    @Data
    public static class ClienteNuevoRequest {
        @NotBlank
        private String nombre;
        @NotBlank
        private String apellido;
        private String rut;
        private String telefono;
        private String email;
        private String direccion;
    }

    @Data
    public static class BicicletaNuevaRequest {
        @NotBlank
        private String marca;
        private String modelo;
        private String tipo;
        private String aro;
        private String color;
        private String numeroSerie;
        private Integer anio;
        private String notas;
    }

    @Data
    public static class ServicioItem {
        @NotNull
        private UUID servicioId;
    }

    @Data
    public static class ProductoItem {
        @NotNull
        private UUID productoId;
        @NotNull
        @Positive
        private Integer cantidad;
    }
}
