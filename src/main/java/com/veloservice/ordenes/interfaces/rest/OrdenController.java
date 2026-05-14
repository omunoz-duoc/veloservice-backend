package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.application.dto.OrdenMetricasResult;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.interfaces.mapper.OrdenMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
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
     */
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody OrdenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                OrdenMapper.toResponse(ordenService.crear(OrdenMapper.toCommand(request)))
        );
    }

    /**
     * Lists all work orders for the current tenant.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<OrdenResponse> ordenes = OrdenMapper.toResponseList(ordenService.listar());
        return ResponseEntity.ok(Map.of(
                "total", ordenes.size(),
                "ordenes", ordenes
        ));
    }

    /**
     * Lists urgent work orders.
     */
    @GetMapping("/urgentes")
    public ResponseEntity<Map<String, Object>> listarUrgentes() {
        List<OrdenResponse> ordenes = OrdenMapper.toResponseList(ordenService.listarUrgentes());
        return ResponseEntity.ok(Map.of(
                "total", ordenes.size(),
                "ordenes", ordenes
        ));
    }

    /**
     * Returns order metrics.
     */
    @GetMapping("/metricas")
    public ResponseEntity<OrdenMetricasResponse> metricas() {
        OrdenMetricasResult result = ordenService.metricas();
        return ResponseEntity.ok(new OrdenMetricasResponse(
                result.getRecibidas(),
                result.getEnProceso(),
                result.getListas(),
                result.getEntregadas()
        ));
    }

    /**
     * Retrieves a work order by identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdenMapper.toResponse(ordenService.obtener(id)));
    }

    /**
     * Changes the state of a work order.
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
            @Valid @RequestBody OrdenServicioRequest request) {
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
            @Valid @RequestBody OrdenProductoRequest request) {
        return ResponseEntity.ok(OrdenMapper.toResponse(
                ordenService.agregarProducto(id, OrdenMapper.toProductoCommand(request))
        ));
    }
}