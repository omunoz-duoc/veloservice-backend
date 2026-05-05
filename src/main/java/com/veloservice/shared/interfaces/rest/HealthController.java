package com.veloservice.shared.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint.
 */
@RestController
public class HealthController {
    /**
     * Returns the service health status.
     *
     * @return status payload
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}