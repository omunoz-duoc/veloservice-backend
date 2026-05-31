package com.veloservice.proveedores_compras.interfaces.mapper;

import com.veloservice.proveedores_compras.application.dto.CompraCreateCommand;
import com.veloservice.proveedores_compras.application.dto.CompraLineaCommand;
import com.veloservice.proveedores_compras.application.dto.CompraLineaResult;
import com.veloservice.proveedores_compras.application.dto.CompraResult;
import com.veloservice.proveedores_compras.interfaces.rest.dto.CompraLineaRequest;
import com.veloservice.proveedores_compras.interfaces.rest.dto.CompraLineaResponse;
import com.veloservice.proveedores_compras.interfaces.rest.dto.CompraRequest;
import com.veloservice.proveedores_compras.interfaces.rest.dto.CompraResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class CompraMapper {
    private CompraMapper() {
    }

    public static CompraCreateCommand toCommand(CompraRequest request) {
        List<CompraLineaCommand> lineas = request.getLineas().stream()
                .map(CompraMapper::toLineaCommand)
                .collect(Collectors.toList());
        return new CompraCreateCommand(
                request.getSucursalProveedorId(),
                request.getNumeroFactura(),
                request.getFechaCompra(),
                request.getNotas(),
                lineas
        );
    }

    public static CompraResponse toResponse(CompraResult result) {
        return CompraResponse.builder()
                .id(result.getId())
                .proveedorNombre(result.getProveedorNombre())
                .numeroFactura(result.getNumeroFactura())
                .neto(result.getNeto())
                .iva(result.getIva())
                .total(result.getTotal())
                .estado(result.getEstado())
                .fechaCompra(result.getFechaCompra())
                .lineas(toLineaResponseList(result.getLineas()))
                .build();
    }

    public static List<CompraResponse> toResponseList(List<CompraResult> results) {
        return results.stream()
                .map(CompraMapper::toResponse)
                .collect(Collectors.toList());
    }

    private static CompraLineaCommand toLineaCommand(CompraLineaRequest request) {
        return new CompraLineaCommand(
                request.getProductoId(),
                request.getCantidad(),
                request.getPrecioUnitario()
        );
    }

    private static CompraLineaResponse toLineaResponse(CompraLineaResult result) {
        return CompraLineaResponse.builder()
                .productoId(result.getProductoId())
                .cantidad(result.getCantidad())
                .precioUnitario(result.getPrecioUnitario())
                .subtotal(result.getSubtotal())
                .build();
    }

    private static List<CompraLineaResponse> toLineaResponseList(List<CompraLineaResult> results) {
        return results.stream()
                .map(CompraMapper::toLineaResponse)
                .collect(Collectors.toList());
    }
}
