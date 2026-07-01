package com.veloservice.finanzas.interfaces.rest;

import com.veloservice.finanzas.application.usecase.CobroService;
import com.veloservice.finanzas.interfaces.rest.dto.MetricasResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.veloservice.finanzas.interfaces.rest.dto.RentabilidadResponse;

import java.util.UUID;

@RestController
@RequestMapping("/finanzas")
@RequiredArgsConstructor
public class FinanzasController {

    private final CobroService cobroService;

    @GetMapping("/metricas")
    public ResponseEntity<MetricasResponse> metricas(@RequestParam(required = false) UUID sucursalId) {
        return ResponseEntity.ok(cobroService.metricas(sucursalId));
    }
    @GetMapping("/rentabilidad")
    public ResponseEntity<RentabilidadResponse> rentabilidad(@RequestParam(required = false) UUID sucursalId) {
        return ResponseEntity.ok(cobroService.rentabilidad(sucursalId));
    }
}
