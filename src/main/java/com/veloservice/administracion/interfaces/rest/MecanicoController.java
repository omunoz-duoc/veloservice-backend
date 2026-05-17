package com.veloservice.administracion.interfaces.rest;

import com.veloservice.administracion.application.usecase.MecanicoService;
import com.veloservice.ordenes.interfaces.mapper.MecanicoMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for mechanics.
 */
@RestController
@RequestMapping("/mecanicos")
@RequiredArgsConstructor
public class MecanicoController {

    private final MecanicoService mecanicoService;

        @GetMapping
    public ResponseEntity<List<MecanicoResponse>> listar(@RequestParam(required = false) Boolean activo) {
        return ResponseEntity.ok(MecanicoMapper.toResponseList(mecanicoService.listar(activo)));
    }
    /**
     * Lists active mechanics for the current tenant.
     *
     * @return available mechanics
     */
    @GetMapping("/disponibles")
    public ResponseEntity<List<MecanicoDisponibleResponse>> listarDisponibles() {
        return ResponseEntity.ok(MecanicoMapper.toDisponibleResponseList(mecanicoService.listarDisponibles()));
    }
}
