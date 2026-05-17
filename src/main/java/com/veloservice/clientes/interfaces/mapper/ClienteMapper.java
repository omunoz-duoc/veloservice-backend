package com.veloservice.clientes.interfaces.mapper;

import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.application.dto.ClienteResult;
import com.veloservice.clientes.application.dto.ClienteResumenResult;
import com.veloservice.clientes.application.dto.MembresiaActualResult;
import com.veloservice.clientes.interfaces.rest.ClienteBusquedaResponse;
import com.veloservice.clientes.interfaces.rest.ClienteRequest;
import com.veloservice.clientes.interfaces.rest.ClienteResponse;
import com.veloservice.clientes.interfaces.rest.ClienteResumenResponse;
import com.veloservice.clientes.interfaces.rest.MembresiaActualResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    private static String buildNombreCompleto(String nombre, String apellido) {
        if (nombre == null) nombre = "";
        if (apellido == null) apellido = "";
        return (nombre + " " + apellido).trim();
    }

    public static ClienteCreateCommand toCommand(ClienteRequest request) {
        return new ClienteCreateCommand(
                request.getNombre(),
                request.getApellido(),
                request.getRut(),
                request.getTelefono(),
                request.getEmail(),
                request.getDireccion()
        );
    }

    public static ClienteResponse toResponse(ClienteResult result) {
        return ClienteResponse.builder()
                .id(result.getId() != null
                        ? "CL-" + result.getId().toString().substring(0, 8).toUpperCase()
                        : null)
                .nombre(result.getNombre())
                .apellido(result.getApellido())
                .tipo(result.getTipo())
                .rut(result.getRut())
                .email(result.getEmail())
                .telefono(result.getTelefono())
                .bicicletasCount(result.getBicicletasCount())
                .ordenesCount(result.getOrdenesCount())
                .totalGastado(result.getTotalGastado())
                .membresiaActual(toMembresiaResponse(result.getMembresiaActual()))
                .build();
    }

    public static List<ClienteResponse> toResponseList(List<ClienteResult> results) {
        return results.stream()
                .map(ClienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static ClienteBusquedaResponse toBusquedaResponse(ClienteResult result) {
        return ClienteBusquedaResponse.builder()
                .id(result.getId())
                .nombreCompleto(buildNombreCompleto(result.getNombre(), result.getApellido()))
                .email(result.getEmail())
                .telefono(result.getTelefono())
                .rut(result.getRut())
                .build();
    }

    public static List<ClienteBusquedaResponse> toBusquedaResponseList(List<ClienteResult> results) {
        return results.stream()
                .map(ClienteMapper::toBusquedaResponse)
              .collect(Collectors.toList());
    }
    
    /**
     * Converts ClienteResumenResult DTO to ClienteResumenResponse for REST.
     * 
     * @param result the ClienteResumenResult DTO
     * @return ClienteResumenResponse REST payload
     */
    public static ClienteResumenResponse toResumenResponse(ClienteResumenResult result) {
        return ClienteResumenResponse.builder()
                .externalId(result.getExternalId())
                .nombreCompleto(result.getNombreCompleto())
                .rut(result.getRut())
                .telefono(result.getTelefono())
                .email(result.getEmail())
                .numeroBicicletas(result.getNumeroBicicletas())
                .numeroOrdenes(result.getNumeroOrdenes())
                .gastoTotal(result.getGastoTotal())
                .build();
    }

    /**
     * Converts a list of ClienteResumenResult DTOs to ClienteResumenResponse list.
     * 
     * @param results list of ClienteResumenResult DTOs
     * @return list of ClienteResumenResponse REST payloads
     */
    public static List<ClienteResumenResponse> toResumenResponseList(List<ClienteResumenResult> results) {
        return results.stream()
                .map(ClienteMapper::toResumenResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts native query result (Object array) to ClienteResumenResult DTO.
     * Expected array order: [externalId, nombreCompleto, rut, telefono, email, 
     *                        numeroBicicletas, numeroOrdenes, gastoTotal]
     * 
     * @param row the Object array from native query result
     * @return ClienteResumenResult DTO
     */
    public static ClienteResumenResult toResumenResult(Object[] row) {
        return ClienteResumenResult.builder()
                .externalId((String) row[0])
                .nombreCompleto((String) row[1])
                .rut((String) row[2])
                .telefono((String) row[3])
                .email((String) row[4])
                .numeroBicicletas(((Number) row[5]).longValue())
                .numeroOrdenes(((Number) row[6]).longValue())
                .gastoTotal(row[7] instanceof BigDecimal ? (BigDecimal) row[7] : new BigDecimal(row[7].toString()))
                .build();
    }

    /**
     * Converts a list of native query results to ClienteResumenResult DTOs.
     * 
     * @param results list of Object arrays from native query
     * @return list of ClienteResumenResult DTOs
     */
    public static List<ClienteResumenResult> toResumenResultList(List<Object[]> results) {
        return results.stream()
                .map(ClienteMapper::toResumenResult)
                .collect(Collectors.toList());
    }

    private static MembresiaActualResponse toMembresiaResponse(MembresiaActualResult result) {
        if (result == null) {
            return null;
        }
        return MembresiaActualResponse.builder()
                .nombre(result.getNombre())
                .descuento(result.getDescuento())
                .build();
    }
}
