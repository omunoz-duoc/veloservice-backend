package com.veloservice.proveedores_compras.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.proveedores_compras.application.usecase.CompraService;
import com.veloservice.proveedores_compras.interfaces.mapper.CompraMapper;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for purchases.
 */
@RestController
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    /**
     * Creates a purchase.
     *
     * @param request purchase request
     * @return created purchase
     */
    @PostMapping
    public ResponseEntity<CompraResponse> crear(@Valid @RequestBody CompraRequest request) {
        return ResponseEntity.ok(CompraMapper.toResponse(
                compraService.crear(CompraMapper.toCommand(request))
        ));
    }

    /**
     * Lists purchases for the current tenant.
     *
     * @return purchases
     */
    @GetMapping
    public ResponseEntity<List<CompraResponse>> listar() {
        return ResponseEntity.ok(CompraMapper.toResponseList(compraService.listar()));
    }

    /**
     * Confirms purchase reception and updates stock.
     *
     * @param id purchase identifier
     * @return updated purchase
     */
    @PutMapping("/{id}/recepcionar")
    public ResponseEntity<CompraResponse> recepcionar(@PathVariable UUID id) {
        return ResponseEntity.ok(CompraMapper.toResponse(compraService.confirmarRecepcion(id)));
    }
}