package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.enums.EstadoGarantiaEnum;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.ordenes.application.dto.GarantiaCreateCommand;
import com.veloservice.ordenes.application.dto.GarantiaResult;
import com.veloservice.ordenes.domain.model.Garantia;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.infraestructure.persistence.repository.GarantiaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GarantiaService {

    private final GarantiaRepository garantiaRepository;
    private final OrdenRepository ordenRepository;
    @Transactional
    public GarantiaResult crearDesdeOrden(GarantiaCreateCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) throw new IllegalStateException("Tenant requerido");

        Orden orden = ordenRepository.findByIdAndSucursalId(command.getOrdenId(), sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!EstadoOrdenEnum.entregada.equals(orden.getEstado())) {
            throw new IllegalArgumentException("Solo se puede crear garantia para orden entregada");
        }

        // número de garantía único: si viene en request validar unicidad, si no generar
        String numero = command.getNumeroGarantia();
        if (numero == null || numero.isBlank()) {
            numero = generarNumeroGarantia();
        } else {
            if (garantiaRepository.findByNumeroGarantia(numero).isPresent()) {
                throw new IllegalArgumentException("Numero de garantia ya existe");
            }
        }

        Garantia g = Garantia.builder()
                .ordenId(command.getOrdenId())
                .numeroGarantia(numero)
                .marcaBicicleta("N/A")
                .componenteAfectado("N/A")
                .descripcionFalla(command.getDescripcionFalla())
                .estado(EstadoGarantiaEnum.abierta)
                .fechaInicio(command.getFechaInicio())
                .fechaVencimiento(command.getFechaVencimiento())
                .condiciones(command.getCondiciones())
                .resolucion(command.getResolucion())
                .build();

        return toResult(garantiaRepository.save(g));
    }

    private String generarNumeroGarantia() {
        int anio = LocalDate.now().getYear();
        long epoch = System.currentTimeMillis();
        return String.format("G-%d-%d", anio, epoch);
    }

    @Transactional(readOnly = true)
    public List<GarantiaResult> listarPorOrden(UUID ordenId) {
        return garantiaRepository.findByOrdenId(ordenId).stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    private GarantiaResult toResult(Garantia g) {
        return GarantiaResult.builder()
                .id(g.getId())
                .ordenId(g.getOrdenId())
                .numeroGarantia(g.getNumeroGarantia())
                .marcaBicicleta(g.getMarcaBicicleta())
                .componenteAfectado(g.getComponenteAfectado())
                .descripcionFalla(g.getDescripcionFalla())
                .estado(g.getEstado())
                .fechaInicio(g.getFechaInicio())
                .fechaVencimiento(g.getFechaVencimiento())
                .condiciones(g.getCondiciones())
                .resolucion(g.getResolucion())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }
}