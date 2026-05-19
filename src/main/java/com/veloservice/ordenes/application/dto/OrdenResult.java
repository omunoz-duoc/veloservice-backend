package com.veloservice.ordenes.application.dto;

import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.config.enums.PrioridadOrdenEnum;
import com.veloservice.config.enums.TipoOrdenEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
public class OrdenResult {
    private UUID id;
    private String externalId;
    private EstadoOrdenEnum estado;
    private TipoOrdenEnum tipo;
    private String diagnosticoInicial;
    private OffsetDateTime fechaIngreso;
    private OffsetDateTime fechaPrometida;

    // Mecánico
    private String mecanicoNombre;
    private String mecanicoApellido;

    // Cliente
    private String clienteNombre;
    private String clienteApellido;
    private String clienteTelefono;

    // Bicicleta
    private String bicicletaMarca;
    private String bicicletaModelo;
    private String bicicletaTipo;
    private String bicicletaColor;
    private String bicicletaTalla;
}
