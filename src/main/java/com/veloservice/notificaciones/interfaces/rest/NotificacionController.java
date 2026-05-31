package com.veloservice.notificaciones.interfaces.rest;

import com.veloservice.notificaciones.application.dto.NotificacionResult;
import com.veloservice.notificaciones.application.usecase.NotificacionService;
import com.veloservice.notificaciones.interfaces.mapper.NotificacionMapper;
import com.veloservice.notificaciones.interfaces.rest.dto.NotificacionRequest;
import com.veloservice.notificaciones.interfaces.rest.dto.NotificacionResponse;

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

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<NotificacionResponse> crear(@Valid @RequestBody NotificacionRequest request) {
        NotificacionResult result = notificacionService.crear(NotificacionMapper.toCommand(request));
        return ResponseEntity.ok(NotificacionMapper.toResponse(result));
    }

    @PutMapping("/{id}/enviar")
    public ResponseEntity<NotificacionResponse> enviar(@PathVariable UUID id) {
        return ResponseEntity.ok(NotificacionMapper.toResponse(notificacionService.enviar(id)));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<NotificacionResponse>> listarPorOrden(@PathVariable UUID ordenId) {
        return ResponseEntity.ok(NotificacionMapper.toResponseList(notificacionService.listarPorOrden(ordenId)));
    }
}