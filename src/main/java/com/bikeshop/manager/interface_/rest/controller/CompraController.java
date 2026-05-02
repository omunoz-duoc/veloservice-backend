package com.bikeshop.manager.interface_.rest.controller;

import com.bikeshop.manager.application.dto.CompraRequest;
import com.bikeshop.manager.application.service.CompraService;
import com.bikeshop.manager.domain.tenant.Compra;
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
    public ResponseEntity<Compra> crear(@Valid @RequestBody CompraRequest request) {
        return ResponseEntity.ok(compraService.crear(request));
    }

    /**
     * Lists purchases for the current tenant.
     *
     * @return purchases
     */
    @GetMapping
    public ResponseEntity<List<Compra>> listar() {
        return ResponseEntity.ok(compraService.listar());
    }

    /**
     * Confirms purchase reception and updates stock.
     *
     * @param id purchase identifier
     * @return updated purchase
     */
    @PutMapping("/{id}/recepcionar")
    public ResponseEntity<Compra> recepcionar(@PathVariable UUID id) {
        return ResponseEntity.ok(compraService.confirmarRecepcion(id));
    }
}
