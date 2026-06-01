package com.veloservice.ordenes.application.dto;

import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.ordenes.domain.PrioridadOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCreateCommand {
    private UUID clienteId;
    private ClienteCreateCommand clienteNuevo;
    private UUID bicicletaId;
    private BicicletaCreateCommand bicicletaNueva;
    private UUID sucursalId;
    private String tipoTrabajo;
    private PrioridadOrdenEnum prioridad;
    private UUID mecanicoId;
    private LocalDate fechaPrometida;
    private String diagnosticoInicial;
    private String observacionesCliente;
    private List<UUID> servicios;
    private List<ProductoItem> productos;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductoItem {
        private UUID productoId;
        private Integer cantidad;
    }
}
