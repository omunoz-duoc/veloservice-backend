package com.veloservice.taller.web;

import com.veloservice.taller.api.MultimediaRequest;
import com.veloservice.taller.internal.service.MultimediaService;
import com.veloservice.taller.internal.entity.Multimedia;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/multimedia")
@RequiredArgsConstructor
public class MultimediaController {

    private final MultimediaService multimediaService;

    @PostMapping("/orden/{ordenId}")
    public ResponseEntity<Multimedia> subir(
            @PathVariable UUID ordenId,
            @RequestParam String etapa,
            @Valid @RequestBody MultimediaRequest request) {
        return ResponseEntity.ok(multimediaService.subir(ordenId, etapa, request));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<Multimedia>> listarPorOrden(@PathVariable UUID ordenId) {
        return ResponseEntity.ok(multimediaService.listarPorOrden(ordenId));
    }
}