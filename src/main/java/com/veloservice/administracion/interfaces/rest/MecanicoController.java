package com.veloservice.administracion.interfaces.rest;

import com.veloservice.administracion.application.usecase.MecanicoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for mechanics.
 */
@RestController
@RequestMapping("/mecanicos")
@RequiredArgsConstructor
public class MecanicoController {

    private final MecanicoService mecanicoService;

    /**
     * Lists active mechanics with their current orders.
     */
    @GetMapping("/activos")
    public ResponseEntity<Map<String, Object>> listarActivos() {
        List<MecanicoResponse> mecanicos = mecanicoService.listarActivos();
        return ResponseEntity.ok(Map.of(
                "total", mecanicos.size(),
                "mecanicos", mecanicos
        ));
    }

    /**
     * Changes mechanic active status.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody MecanicoEstadoRequest request) {
        mecanicoService.cambiarEstado(id, request.getActivo());
        return ResponseEntity.noContent().build();
    }

    /**
     * Changes mechanic role.
     */
    @PutMapping("/{id}/rol")
    public ResponseEntity<Void> cambiarRol(
            @PathVariable UUID id,
            @Valid @RequestBody MecanicoRolRequest request) {
        mecanicoService.cambiarRol(id, request.getRol());
        return ResponseEntity.noContent().build();
    }
}
