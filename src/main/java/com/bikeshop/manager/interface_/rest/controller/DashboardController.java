package com.bikeshop.manager.interface_.rest.controller;

import com.bikeshop.manager.application.dto.DashboardAlertasResponse;
import com.bikeshop.manager.application.dto.DashboardHoyResponse;
import com.bikeshop.manager.application.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/hoy")
    public ResponseEntity<DashboardHoyResponse> hoy() {
        return ResponseEntity.ok(dashboardService.resumenHoy());
    }

    @GetMapping("/estados")
    public ResponseEntity<Map<String, Long>> estados() {
        return ResponseEntity.ok(dashboardService.estadoOrdenes());
    }

    @GetMapping("/alertas")
    public ResponseEntity<DashboardAlertasResponse> alertas() {
        return ResponseEntity.ok(dashboardService.alertas());
    }
}
