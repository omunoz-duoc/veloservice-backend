package com.veloservice.taller.web;

import com.veloservice.taller.api.DashboardAlertasResponse;
import com.veloservice.taller.api.DashboardHoyResponse;
import com.veloservice.taller.internal.service.DashboardService;
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