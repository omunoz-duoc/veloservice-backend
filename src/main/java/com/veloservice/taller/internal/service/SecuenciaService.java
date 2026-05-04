package com.veloservice.taller.internal.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Generates unique tenant sequences for work orders.
 */
@Service
public class SecuenciaService {

    /**
     * Generates the next work order number for the given workshop.
     *
     * @param sucursalId branch identifier
     * @return formatted work order number
     */
    public String generarNumeroOrden(UUID sucursalId) {
        int anio = LocalDate.now().getYear();
        long epoch = System.currentTimeMillis();
        return String.format("OT-%d-%d", anio, epoch);
    }
}