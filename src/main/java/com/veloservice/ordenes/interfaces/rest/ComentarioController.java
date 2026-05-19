package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ordenes")
public class ComentarioController {

    private final ComentarioService comentarioService;
    private final OrdenService ordenService;

    public ComentarioController(ComentarioService comentarioService, OrdenService ordenService) {
        this.comentarioService = comentarioService;
        this.ordenService = ordenService;
    }

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<Map<String, List<ComentarioResult>>> listarComentarios(@PathVariable String id) {
        var ordenId = ordenService.resolveOrdenId(id);
        var comentarios = comentarioService.listarComentariosPorOrden(ordenId);
        return ResponseEntity.ok(Map.of("comentarios", comentarios));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResult> agregarComentario(
            @PathVariable String id,
            @RequestBody ComentarioRequest request) {
        var ordenId = ordenService.resolveOrdenId(id);
        var result = comentarioService.agregarComentario(ordenId, request);
        return ResponseEntity.ok(result);
    }
}
