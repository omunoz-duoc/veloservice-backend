package com.veloservice.servicios.interfaces.rest;

import com.veloservice.servicios.application.usecase.ServicioService;
import com.veloservice.servicios.interfaces.mapper.ServicioMapper;

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
        return ResponseEntity.ok(ServicioMapper.toResponseList(servicioService.listar()));
    }

    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(ServicioMapper.toResponse(
                servicioService.crear(ServicioMapper.toCommand(request))
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(ServicioMapper.toResponse(
                servicioService.actualizar(id, ServicioMapper.toCommand(request))
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        servicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sucursal")
    public ResponseEntity<Void> asignarPrecioSucursal(@Valid @RequestBody SucursalServicioRequest request) {
        servicioService.asignarPrecioSucursal(ServicioMapper.toSucursalCommand(request));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sucursal")
    public ResponseEntity<List<SucursalServicioResponse>> listarSucursal() {
        return ResponseEntity.ok(ServicioMapper.toSucursalResponseList(servicioService.listarServiciosSucursal()));
    }
}