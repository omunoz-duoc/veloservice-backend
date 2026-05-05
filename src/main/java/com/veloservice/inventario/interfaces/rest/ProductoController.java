package com.veloservice.inventario.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.inventario.application.usecase.ProductoService;
import com.veloservice.inventario.interfaces.mapper.ProductoMapper;

import java.util.List;

/**
 * REST endpoints for products.
 */
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Creates a product.
     *
     * @param request product request
     * @return created product
     */
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ProductoMapper.toResponse(
                productoService.crear(ProductoMapper.toCommand(request))
        ));
    }

    /**
     * Lists products for the current tenant.
     *
     * @return product list
     */
    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar() {
        return ResponseEntity.ok(ProductoMapper.toResponseList(productoService.listar()));
    }

    /**
     * Lists products with low stock.
     *
     * @return product alerts
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<ProductoResponse>> alertas() {
        return ResponseEntity.ok(ProductoMapper.toResponseList(productoService.alertasStockBajo()));
    }
}