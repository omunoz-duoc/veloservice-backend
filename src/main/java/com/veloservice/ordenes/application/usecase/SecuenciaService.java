package com.veloservice.ordenes.application.usecase;

import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates unique tenant sequences for work orders.
 */
@Service
@RequiredArgsConstructor
public class SecuenciaService {

    private final OrdenRepository ordenRepository;

    /**
     * Generates the next work order number for the given workshop.
     *
     * @param sucursalId branch identifier
     * @return formatted work order number
     */
    public String generarNumeroOrden(UUID tallerId) {
        String numeroOrden;
        int intentos = 0;
        do {
            int numero = ThreadLocalRandom.current().nextInt(0, 100000);
            numeroOrden = String.format("OT-%05d", numero);
            intentos++;
        } while (ordenRepository.existsByNumeroOrdenAndTallerId(numeroOrden, tallerId) && intentos < 20);

        if (ordenRepository.existsByNumeroOrdenAndTallerId(numeroOrden, tallerId)) {
            throw new IllegalStateException("No se pudo generar numero de orden unico");
        }
        return numeroOrden;
    }
}