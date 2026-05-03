package com.bikeshop.manager.interface_.rest.controller;

import com.bikeshop.manager.application.dto.EstadoChangeRequest;
import com.bikeshop.manager.application.dto.OrdenRequest;
import com.bikeshop.manager.application.dto.OrdenResponse;
import com.bikeshop.manager.application.service.OrdenService;
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
 * REST endpoints for work orders.
 */
@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenService;

    /**
     * Creates a new work order.
     *
     * @param request work order request
     * @return created work order
     */
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody OrdenRequest request) {
        return ResponseEntity.ok(ordenService.crear(request));
    }

    /**
     * Lists work orders for the current tenant.
     *
     * @return work orders
     */
    @GetMapping
    public ResponseEntity<List<OrdenResponse>> listar() {
        return ResponseEntity.ok(ordenService.listar());
    }

    /**
     * Retrieves a work order by identifier.
     *
     * @param id work order identifier
     * @return work order
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(ordenService.obtener(id));
    }

    /**
     * Changes the state of a work order.
     *
     * @param id work order identifier
     * @param request state change request
     * @return updated work order
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<OrdenResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody EstadoChangeRequest request) {
        return ResponseEntity.ok(ordenService.cambiarEstado(id, request));
    }

    /**
     * Agrega un servicio a la orden.
     */
    @PostMapping("/{id}/servicios")
    public ResponseEntity<OrdenResponse> agregarServicio(
            @PathVariable UUID id,
            @Valid @RequestBody com.bikeshop.manager.application.dto.OrdenServicioRequest request) {
        return ResponseEntity.ok(ordenService.agregarServicio(id, request));
    }

    /**
     * Agrega un producto a la orden.
     */
    @PostMapping("/{id}/productos")
    public ResponseEntity<OrdenResponse> agregarProducto(
            @PathVariable UUID id,
            @Valid @RequestBody com.bikeshop.manager.application.dto.OrdenProductoRequest request) {
        return ResponseEntity.ok(ordenService.agregarProducto(id, request));
    }
}
