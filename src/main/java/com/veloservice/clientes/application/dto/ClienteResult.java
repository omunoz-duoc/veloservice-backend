package com.veloservice.clientes.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Application result for customer queries.
 */
@Data
@Builder
@AllArgsConstructor
public class ClienteResult {
    private UUID id;
    private String nombre;
    private String apellido;
    private String rut;
    private String telefono;
    private String email;
    private String direccion;
    private MembresiaActualResult membresiaActual;
    private String tipo;
    private int bicicletasCount;
    private int ordenesCount;
    private BigDecimal totalGastado;
}