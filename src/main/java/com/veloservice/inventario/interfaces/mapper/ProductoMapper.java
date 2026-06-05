package com.veloservice.inventario.interfaces.mapper;

import com.veloservice.inventario.application.dto.ProductoCreateCommand;
import com.veloservice.inventario.application.dto.ProductoResult;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.interfaces.rest.dto.ProductoRequest;
import com.veloservice.inventario.interfaces.rest.dto.ProductoResponse;

import java.util.List;
import java.util.stream.Collectors;

public final class ProductoMapper {
    private ProductoMapper() {
    }

    public static ProductoResult toResult(Producto producto) {
        return ProductoResult.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .sku(producto.getSku())
                .marca(producto.getMarca())
                .precioCosto(producto.getPrecioCosto())
                .precioVenta(producto.getPrecioVenta())
                .stock(producto.getStock())
                .stockMinimo(producto.getStockMinimo())
                .alertaStockBajo(producto.getStock() <= producto.getStockMinimo())
                .build();
    }

    public static ProductoCreateCommand toCommand(ProductoRequest request) {
        return new ProductoCreateCommand(
                request.getNombre(),
                request.getSku(),
                request.getMarca(),
                request.getUnidadMedida(),
                request.getPrecioCosto(),
                request.getPrecioVenta(),
                request.getStock(),
                request.getStockMinimo(),
                request.getCategoriaId()
        );
    }

    public static ProductoResponse toResponse(ProductoResult result) {
        return ProductoResponse.builder()
                .id(result.getId())
                .nombre(result.getNombre())
                .sku(result.getSku())
                .marca(result.getMarca())
                .categoria(result.getCategoriaNombre())
                .precioCosto(result.getPrecioCosto())
                .precioVenta(result.getPrecioVenta())
                .stock(result.getStock())
                .build();
    }

    public static List<ProductoResponse> toResponseList(List<ProductoResult> results) {
        return results.stream()
                .map(ProductoMapper::toResponse)
                .collect(Collectors.toList());
    }
}
