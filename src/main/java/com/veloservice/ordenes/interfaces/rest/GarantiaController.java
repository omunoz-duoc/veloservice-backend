package com.veloservice.ordenes.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.ordenes.application.usecase.GarantiaService;
import com.veloservice.ordenes.interfaces.mapper.GarantiaMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/garantias")
@RequiredArgsConstructor
public class GarantiaController {

    private final GarantiaService garantiaService;

    @PostMapping
    public ResponseEntity<GarantiaResponse> crear(@Valid @RequestBody GarantiaRequest request) {
        return ResponseEntity.ok(GarantiaMapper.toResponse(
                garantiaService.crearDesdeOrden(GarantiaMapper.toCommand(request))
        ));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<GarantiaResponse>> listarPorOrden(@PathVariable UUID ordenId) {
        return ResponseEntity.ok(GarantiaMapper.toResponseList(garantiaService.listarPorOrden(ordenId)));
    }
}