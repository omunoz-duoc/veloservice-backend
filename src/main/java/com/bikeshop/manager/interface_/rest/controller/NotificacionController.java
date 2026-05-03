package com.bikeshop.manager.interface_.rest.controller;

import com.bikeshop.manager.application.service.NotificacionService;
import com.bikeshop.manager.domain.tenant.Notificacion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<Notificacion> crear(@Valid @RequestBody Notificacion request) {
        return ResponseEntity.ok(notificacionService.crear(request));
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<Notificacion> enviar(@PathVariable UUID id) {
        return ResponseEntity.ok(notificacionService.enviar(id));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<Notificacion>> listarPorOrden(@PathVariable UUID ordenId) {
        return ResponseEntity.ok(notificacionService.listarPorOrden(ordenId));
    }
}
