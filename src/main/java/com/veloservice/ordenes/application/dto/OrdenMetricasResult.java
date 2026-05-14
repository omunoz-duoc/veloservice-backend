package com.veloservice.ordenes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Application result for order metrics.
 */
@Data
@AllArgsConstructor
public class OrdenMetricasResult {
    private long recibidas;
    private long enProceso;
    private long listas;
    private long entregadas;
}
