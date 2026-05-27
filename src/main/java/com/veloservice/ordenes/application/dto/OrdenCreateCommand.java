package com.veloservice.ordenes.application.dto;

import com.veloservice.ordenes.domain.TipoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Application command for creating a work order.
 */
@Data
@AllArgsConstructor
public class OrdenCreateCommand {
    private UUID bicicletaId;
    private TipoOrdenEnum tipo;
    private String diagnosticoInicial;
    private String observacionesCliente;
    private List<MultimediaCreateCommand> multimedia;
}
