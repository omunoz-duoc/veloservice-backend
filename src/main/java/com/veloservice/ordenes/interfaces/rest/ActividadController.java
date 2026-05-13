package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.interfaces.mapper.OrdenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/actividad")
@RequiredArgsConstructor
public class ActividadController {

    private final OrdenService ordenService;

    @GetMapping("/reciente")
    public ResponseEntity<List<OrdenActividadRecienteResponse>> reciente() {
        return ResponseEntity.ok(OrdenMapper.toActividadRecienteResponseList(ordenService.listarActividadReciente()));
    }
}
