package com.veloservice.finanzas.interfaces.rest;

import com.veloservice.finanzas.application.usecase.CobroService;
import com.veloservice.finanzas.interfaces.rest.dto.MetricasResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.veloservice.finanzas.interfaces.rest.dto.RentabilidadResponse;

@RestController
@RequestMapping("/finanzas")
@RequiredArgsConstructor
public class FinanzasController {

    private final CobroService cobroService;

    @GetMapping("/metricas")
    public ResponseEntity<MetricasResponse> metricas() {
        return ResponseEntity.ok(MetricasResponse.builder()
                .cobrosDelDia(cobroService.ingresosHoy())
                .build());
    }
    @GetMapping("/rentabilidad")
    public ResponseEntity<RentabilidadResponse> rentabilidad() {
        return ResponseEntity.ok(cobroService.rentabilidad());
}
}
