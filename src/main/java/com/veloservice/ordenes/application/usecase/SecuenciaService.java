package com.veloservice.ordenes.application.usecase;

import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecuenciaService {

    private static final Pattern NUMERO_ORDEN_PATTERN = Pattern.compile("^OT-(\\d+)$");

    private final OrdenRepository ordenRepository;

    public synchronized String generarNumeroOrden(UUID tallerId) {
        int siguiente = ordenRepository.findNumerosOrdenByTallerId(tallerId).stream()
                .map(NUMERO_ORDEN_PATTERN::matcher)
                .filter(java.util.regex.Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0) + 1;

        String numeroOrden;
        do {
            numeroOrden = String.format("OT-%05d", siguiente++);
        } while (ordenRepository.existsByNumeroOrdenAndTallerId(numeroOrden, tallerId));

        return numeroOrden;
    }
}
