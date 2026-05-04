package com.veloservice.taller.internal.service;

import com.veloservice.taller.api.GarantiaRequest;
import com.veloservice.config.enums.EstadoGarantiaEnum;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.taller.internal.entity.Garantia;
import com.veloservice.taller.internal.entity.Orden;
import com.veloservice.taller.internal.repository.GarantiaRepository;
import com.veloservice.taller.internal.repository.OrdenRepository;
import com.veloservice.config.security.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GarantiaService {

    private final GarantiaRepository garantiaRepository;
    private final OrdenRepository ordenRepository;
    @Transactional
    public Garantia crearDesdeOrden(GarantiaRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) throw new IllegalStateException("Tenant requerido");

        Orden orden = ordenRepository.findByIdAndSucursalId(request.getOrdenId(), sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!EstadoOrdenEnum.entregada.equals(orden.getEstado())) {
            throw new IllegalArgumentException("Solo se puede crear garantia para orden entregada");
        }

        // número de garantía único: si viene en request validar unicidad, si no generar
        String numero = request.getNumeroGarantia();
        if (numero == null || numero.isBlank()) {
            numero = generarNumeroGarantia();
        } else {
            if (garantiaRepository.findByNumeroGarantia(numero).isPresent()) {
                throw new IllegalArgumentException("Numero de garantia ya existe");
            }
        }

        Garantia g = Garantia.builder()
                .ordenId(request.getOrdenId())
                .numeroGarantia(numero)
                .marcaBicicleta("N/A")
                .componenteAfectado("N/A")
                .descripcionFalla(request.getDescripcionFalla())
                .estado(EstadoGarantiaEnum.abierta)
                .fechaInicio(request.getFechaInicio())
                .fechaVencimiento(request.getFechaVencimiento())
                .condiciones(request.getCondiciones())
                .resolucion(request.getResolucion())
                .build();

        return garantiaRepository.save(g);
    }

    private String generarNumeroGarantia() {
        int anio = LocalDate.now().getYear();
        long epoch = System.currentTimeMillis();
        return String.format("G-%d-%d", anio, epoch);
    }

    @Transactional(readOnly = true)
    public List<Garantia> listarPorOrden(UUID ordenId) {
        return garantiaRepository.findByOrdenId(ordenId);
    }
}