package com.veloservice.ordenes.interfaces.rest;

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

import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.interfaces.mapper.OrdenMapper;

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
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody NuevaOrdenRequest request) {
        return ResponseEntity.ok(OrdenMapper.toResponse(
                ordenService.crearNuevaOrden(OrdenMapper.toCommand(request))
        ));
    }

    /**
     * Lists work orders for the current tenant.
     *
     * @return work orders
     */
    @GetMapping
    public ResponseEntity<List<OrdenResumenResponse>> listar() {
        return ResponseEntity.ok(OrdenMapper.toResumenResponseList(ordenService.listar()));
    }

    /**
     * Lists orders ready for delivery.
     *
     * @return orders ready for delivery
     */
    @GetMapping("/lista-entrega")
    public ResponseEntity<List<OrdenListaEntregaResponse>> listarListaEntrega() {
        return ResponseEntity.ok(OrdenMapper.toListaEntregaResponseList(ordenService.listarListaEntrega()));
    }

    /**
     * Retrieves a work order by identifier.
     *
     * @param id work order identifier
     * @return work order
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdenMapper.toResponse(ordenService.obtener(id)));
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
        return ResponseEntity.ok(OrdenMapper.toResponse(
            ordenService.cambiarEstado(id, OrdenMapper.toEstadoChangeCommand(request))
        ));
    }

    /**
     * Agrega un servicio a la orden.
     */
    @PostMapping("/{id}/servicios")
    public ResponseEntity<OrdenResponse> agregarServicio(
            @PathVariable UUID id,
            @Valid @RequestBody com.veloservice.ordenes.interfaces.rest.OrdenServicioRequest request) {
        return ResponseEntity.ok(OrdenMapper.toResponse(
            ordenService.agregarServicio(id, OrdenMapper.toServicioCommand(request))
        ));
    }

    /**
     * Agrega un producto a la orden.
     */
    @PostMapping("/{id}/productos")
    public ResponseEntity<OrdenResponse> agregarProducto(
            @PathVariable UUID id,
            @Valid @RequestBody com.veloservice.ordenes.interfaces.rest.OrdenProductoRequest request) {
        return ResponseEntity.ok(OrdenMapper.toResponse(
                ordenService.agregarProducto(id, OrdenMapper.toProductoCommand(request))
        ));
    }
}