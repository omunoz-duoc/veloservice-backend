package com.veloservice.ordenes.application.usecase;

import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.ordenes.application.dto.DashboardAlertasResult;
import com.veloservice.ordenes.application.dto.DashboardHoyResult;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.security.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrdenRepository ordenRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
        public DashboardHoyResult resumenHoy() {
        var sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
                        return DashboardHoyResult.builder()
                    .ordenesRecibidas(0)
                    .ordenesEntregadas(0)
                    .ingresosHoy(java.math.BigDecimal.ZERO)
                    .build();
        }

        LocalDate hoy = LocalDate.now();
        List<Orden> ordenes = ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId);

        long recibidas = ordenes.stream()
                .filter(o -> o.getFechaIngreso() != null && hoy.equals(o.getFechaIngreso().toLocalDate()))
                .count();

        long entregadas = ordenes.stream()
                .filter(o -> o.getFechaEntrega() != null && hoy.equals(o.getFechaEntrega().toLocalDate()))
                .count();

        return DashboardHoyResult.builder()
                .ordenesRecibidas(recibidas)
                .ordenesEntregadas(entregadas)
                .ingresosHoy(java.math.BigDecimal.ZERO)
                .build();
    }

    @Transactional(readOnly = true)
        public Map<String, Long> estadoOrdenes() {
        var sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return Map.of();
        }
        return ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId).stream()
                .collect(Collectors.groupingBy(o -> o.getEstado().name(), Collectors.counting()));
    }

    @Transactional(readOnly = true)
    public DashboardAlertasResult alertas() {
        var sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return DashboardAlertasResult.builder()
                    .productosStockBajo(List.of())
                    .ordenesAtrasadas(List.of())
                    .build();
        }

        var productosStockBajo = productoRepository.findBySucursalIdAndStockLessThanEqualStockMinimo(sucursalId)
                .stream()
                .map(p -> DashboardAlertasResult.StockBajoItem.builder()
                        .productoId(p.getId())
                        .nombre(p.getNombre())
                        .stock(p.getStock())
                        .stockMinimo(p.getStockMinimo())
                        .build())
                .toList();

        OffsetDateTime ahora = OffsetDateTime.now();
        var ordenesAtrasadas = ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId).stream()
                .filter(o -> o.getFechaPrometida() != null && o.getFechaPrometida().isBefore(ahora))
                .filter(o -> !EstadoOrdenEnum.entregada.equals(o.getEstado()))
                .filter(o -> !EstadoOrdenEnum.cancelada.equals(o.getEstado()))
                .map(o -> DashboardAlertasResult.OrdenAtrasadaItem.builder()
                        .ordenId(o.getId())
                        .numeroOrden(o.getNumeroOrden())
                        .estado(o.getEstado())
                        .fechaPrometida(o.getFechaPrometida())
                        .build())
                .toList();

        return DashboardAlertasResult.builder()
                .productosStockBajo(productosStockBajo)
                .ordenesAtrasadas(ordenesAtrasadas)
                .build();
    }

}