package com.veloservice.inventario.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductosListResponse(List<ProductoListItem> productos) {
    public record ProductoListItem(UUID id, String nombre, BigDecimal precioVenta, Integer stock) {}
}
