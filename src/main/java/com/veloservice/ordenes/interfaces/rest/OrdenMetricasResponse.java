package com.veloservice.ordenes.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrdenMetricasResponse {
    private long recibidas;
    @JsonProperty("en_proceso")
    private long enProceso;
    private long listas;
    private long entregadas;
}