package com.veloservice.taller.web;

import com.veloservice.taller.api.GarantiaRequest;
import com.veloservice.taller.internal.service.GarantiaService;
import com.veloservice.taller.internal.entity.Garantia;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/garantias")
@RequiredArgsConstructor
public class GarantiaController {

    private final GarantiaService garantiaService;

    @PostMapping
    public ResponseEntity<Garantia> crear(@Valid @RequestBody GarantiaRequest request) {
        return ResponseEntity.ok(garantiaService.crearDesdeOrden(request));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<Garantia>> listarPorOrden(@PathVariable UUID ordenId) {
        return ResponseEntity.ok(garantiaService.listarPorOrden(ordenId));
    }
}