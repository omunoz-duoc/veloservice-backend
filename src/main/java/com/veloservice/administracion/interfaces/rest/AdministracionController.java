package com.veloservice.administracion.interfaces.rest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.administracion.application.port.UsuarioPort;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/administracion")
@RequiredArgsConstructor
public class AdministracionController {

    private final UsuarioPort usuarioPort;
    private final SucursalRepository sucursalRepository;

    @GetMapping("/lista-mecanicos")
    @PreAuthorize("hasRole('admin_taller') or hasRole('jefe_taller') or hasRole('recepcionista')")
    public ResponseEntity<List<MecanicoResponse>> listaMecanicos(
            @RequestParam(required = false) UUID sucursalId) {

        UUID resolvedSucursalId;

        if (sucursalId != null) {
            if (!sucursalRepository.existsByIdAndTallerId(sucursalId, TallerContext.getCurrentTaller())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            resolvedSucursalId = sucursalId;
        } else {
            resolvedSucursalId = SucursalContext.getCurrentSucursal();
        }

        if (resolvedSucursalId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<MecanicoResponse> mecanicos = usuarioPort.findMecanicosBySucursalId(resolvedSucursalId)
                .stream()
                .map(u -> new MecanicoResponse(u.id().toString(), u.nombre() + " " + u.apellido()))
                .toList();

        return ResponseEntity.ok(mecanicos);
    }

    record MecanicoResponse(String id, String nombre) {}
}
