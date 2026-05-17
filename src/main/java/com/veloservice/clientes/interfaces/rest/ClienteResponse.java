package com.veloservice.clientes.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Customer response payload.
 */
@Data
@Builder
@AllArgsConstructor
public class ClienteResponse {
    private String id;
    private String nombre;
    private String apellido;
    private String tipo;
    private String rut;
    private String email;
    private String telefono;
    @JsonProperty("bicicletas_count")
    private int bicicletasCount;
    @JsonProperty("ordenes_count")
    private int ordenesCount;
    @JsonProperty("total_gastado")
    private BigDecimal totalGastado;
    private MembresiaActualResponse membresiaActual;
}