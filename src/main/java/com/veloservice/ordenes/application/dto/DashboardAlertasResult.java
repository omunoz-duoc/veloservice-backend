package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.EstadoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application result for dashboard alerts.
 */
@Data
@Builder
@AllArgsConstructor
public class DashboardAlertasResult {
    private List<StockBajoItem> productosStockBajo;
    private List<OrdenAtrasadaItem> ordenesAtrasadas;

    @Data
    @Builder
    @AllArgsConstructor
    public static class StockBajoItem {
        private UUID productoId;
        private String nombre;
        private Integer stock;
        private Integer stockMinimo;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class OrdenAtrasadaItem {
        private UUID ordenId;
        private String numeroOrden;
        private EstadoOrdenEnum estado;
        private OffsetDateTime fechaPrometida;
    }
}
