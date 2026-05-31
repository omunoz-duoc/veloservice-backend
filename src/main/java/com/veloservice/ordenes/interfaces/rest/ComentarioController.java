package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.interfaces.rest.dto.ComentarioRequest;

import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/ordenes")
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<Map<String, List<ComentarioResult>>> listarComentarios(@PathVariable UUID id) {
        var comentarios = comentarioService.listarComentariosPorOrden(id);
        return ResponseEntity.ok(Map.of("comentarios", comentarios));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResult> agregarComentario(
            @PathVariable UUID id,
            @RequestBody ComentarioRequest request) {
        var result = comentarioService.agregarComentario(id, request);
        return ResponseEntity.ok(result);
    }
}
