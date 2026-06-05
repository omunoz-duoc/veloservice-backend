package com.veloservice.auth.interfaces.rest;

import com.veloservice.auth.application.usecase.MecanicoService;
import com.veloservice.auth.application.dto.MecanicoResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mecanicos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('mecanico') or hasRole('recepcionista') or hasRole('jefe_taller') or hasRole('admin_taller')")
public class MecanicoController {

    private final MecanicoService mecanicoService;

    @GetMapping("/activos")
    public ResponseEntity<List<MecanicoResult>> listarActivos() {
        return ResponseEntity.ok(mecanicoService.listarActivos());
    }
}
