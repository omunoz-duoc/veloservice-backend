package com.bikeshop.manager.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class DashboardAlertasResponse {
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
        private String estado;
        private LocalDateTime fechaPrometida;
    }
}
