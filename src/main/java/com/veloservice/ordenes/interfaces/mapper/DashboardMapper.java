package com.veloservice.ordenes.interfaces.mapper;

import com.veloservice.ordenes.application.dto.DashboardAlertasResult;
import com.veloservice.ordenes.application.dto.DashboardHoyResult;
import com.veloservice.ordenes.application.usecase.DashboardService;
import com.veloservice.ordenes.interfaces.rest.DashboardAlertasResponse;
import com.veloservice.ordenes.interfaces.rest.DashboardFinanzasHoyResponse;
import com.veloservice.ordenes.interfaces.rest.DashboardHoyResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class DashboardMapper {
    private DashboardMapper() {
    }

    public static DashboardHoyResponse toResponse(DashboardHoyResult result) {
        return DashboardHoyResponse.builder()
                .ordenesRecibidas(result.getOrdenesRecibidas())
                .ordenesEntregadas(result.getOrdenesEntregadas())
                .ingresosHoy(result.getIngresosHoy())
                .build();
    }

    public static DashboardAlertasResponse toResponse(DashboardAlertasResult result) {
        List<DashboardAlertasResponse.StockBajoItem> productos = result.getProductosStockBajo().stream()
                .map(item -> DashboardAlertasResponse.StockBajoItem.builder()
                        .productoId(item.getProductoId())
                        .nombre(item.getNombre())
                        .stock(item.getStock())
                        .stockMinimo(item.getStockMinimo())
                        .build())
                .collect(Collectors.toList());

        List<DashboardAlertasResponse.OrdenAtrasadaItem> ordenes = result.getOrdenesAtrasadas().stream()
                .map(item -> DashboardAlertasResponse.OrdenAtrasadaItem.builder()
                        .ordenId(item.getOrdenId())
                        .numeroOrden(item.getNumeroOrden())
                        .estado(item.getEstado())
                        .fechaPrometida(item.getFechaPrometida())
                        .build())
                .collect(Collectors.toList());

        return DashboardAlertasResponse.builder()
                .productosStockBajo(productos)
                .ordenesAtrasadas(ordenes)
                .build();
    }

        public static DashboardFinanzasHoyResponse toResponse(DashboardService.DashboardFinanzasHoyResult result) {
                return new DashboardFinanzasHoyResponse(
                                result.totalIngresosHoy(),
                                result.totalCobrosHoy(),
                                result.metodoPagoMasUsado()
                );
        }
}
