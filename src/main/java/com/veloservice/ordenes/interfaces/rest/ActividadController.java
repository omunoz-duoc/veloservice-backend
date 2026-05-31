package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.application.usecase.OrdenService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/actividad")
@RequiredArgsConstructor
public class ActividadController {

    private final OrdenService ordenService;

    // @GetMapping("/reciente")
    // public ResponseEntity<List<OrdenActividadRecienteResponse>> reciente() {
    //     return ResponseEntity.ok(ordenService.listarActividadReciente());
    // }
}
