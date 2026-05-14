package com.veloservice.ordenes.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response payload for order metrics.
 */
@Data
@AllArgsConstructor
public class OrdenMetricasResponse {
    private long recibidas;
    private long enProceso;
    private long listas;
    private long entregadas;
}
