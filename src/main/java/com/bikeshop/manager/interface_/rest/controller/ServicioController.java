package com.bikeshop.manager.interface_.rest.controller;

import com.bikeshop.manager.application.dto.ServicioRequest;
import com.bikeshop.manager.application.dto.ServicioResponse;
import com.bikeshop.manager.application.dto.SucursalServicioRequest;
import com.bikeshop.manager.application.service.ServicioService;
import com.bikeshop.manager.domain.tenant.SucursalServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping
    public ResponseEntity<List<ServicioResponse>> listar() {
        return ResponseEntity.ok(servicioService.listar());
    }

    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(servicioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(servicioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        servicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sucursal")
    public ResponseEntity<Void> asignarPrecioSucursal(@Valid @RequestBody SucursalServicioRequest request) {
        servicioService.asignarPrecioSucursal(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sucursal")
    public ResponseEntity<List<SucursalServicio>> listarSucursal() {
        return ResponseEntity.ok(servicioService.listarServiciosSucursal());
    }
}
