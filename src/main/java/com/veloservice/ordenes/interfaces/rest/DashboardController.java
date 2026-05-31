package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.interfaces.rest.dto.DashboardHoyResponse;
import com.veloservice.ordenes.interfaces.rest.dto.DashboardAlertasResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.ordenes.application.usecase.DashboardService;
import com.veloservice.ordenes.interfaces.mapper.DashboardMapper;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/hoy")
    public ResponseEntity<DashboardHoyResponse> hoy() {
        return ResponseEntity.ok(DashboardMapper.toResponse(dashboardService.resumenHoy()));
    }

    @GetMapping("/estados")
    public ResponseEntity<Map<String, Long>> estados() {
        return ResponseEntity.ok(dashboardService.estadoOrdenes());
    }

    @GetMapping("/alertas")
    public ResponseEntity<DashboardAlertasResponse> alertas() {
        return ResponseEntity.ok(DashboardMapper.toResponse(dashboardService.alertas()));
    }

}