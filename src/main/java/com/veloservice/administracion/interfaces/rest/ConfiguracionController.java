package com.veloservice.administracion.interfaces.rest;

import com.veloservice.administracion.application.dto.TallerResult;
import com.veloservice.administracion.application.usecase.TallerService;
import com.veloservice.administracion.interfaces.rest.dto.ConfiguracionTallerRequest;
import com.veloservice.administracion.interfaces.rest.dto.ConfiguracionTallerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/configuracion")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin_taller')")
public class ConfiguracionController {

    private final TallerService tallerService;

    @GetMapping("/taller")
    public ResponseEntity<ConfiguracionTallerResponse> obtenerTaller() {
        return ResponseEntity.ok(toResponse(tallerService.obtenerActual()));
    }

    @PatchMapping("/taller")
    public ResponseEntity<ConfiguracionTallerResponse> actualizarTaller(
            @RequestBody ConfiguracionTallerRequest request
    ) {
        TallerResult result = tallerService.actualizarActual(
                request.nombre(),
                request.rut(),
                request.telefono(),
                request.email(),
                request.logoUrl()
        );
        return ResponseEntity.ok(toResponse(result));
    }

    private ConfiguracionTallerResponse toResponse(TallerResult result) {
        return new ConfiguracionTallerResponse(
                result.getNombre(),
                result.getRut(),
                result.getTelefono(),
                result.getEmail(),
                result.getLogoUrl()
        );
    }
}
