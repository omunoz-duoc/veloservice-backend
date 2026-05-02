package com.bikeshop.manager.application.service;

import com.bikeshop.manager.domain.platform.SecuenciaTaller;
import com.bikeshop.manager.infrastructure.persistence.repository.SecuenciaTallerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Generates unique tenant sequences for work orders.
 */
@Service
@RequiredArgsConstructor
public class SecuenciaService {

    private final SecuenciaTallerRepository secuenciaRepository;

    /**
     * Generates the next work order number for the given tenant.
     *
     * @param tallerId tenant identifier
     * @return formatted work order number
     */
    @Transactional
    public String generarNumeroOrden(UUID tallerId) {
        int anio = LocalDate.now().getYear();

        SecuenciaTaller secuencia = secuenciaRepository
                .findByTallerIdAndTipoAndAnio(tallerId, "OT", anio)
                .orElseGet(() -> secuenciaRepository.save(SecuenciaTaller.builder()
                        .tallerId(tallerId)
                        .tipo("OT")
                        .anio(anio)
                        .ultimoNumero(0)
                        .build()));

        int siguiente = secuencia.getUltimoNumero() + 1;
        secuencia.setUltimoNumero(siguiente);
        secuenciaRepository.save(secuencia);

        return String.format("OT-%d-%d", anio, siguiente);
    }
}
