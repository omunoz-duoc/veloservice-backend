package com.bikeshop.manager.interface_.rest.controller;

import com.bikeshop.manager.application.dto.CobroRequest;
import com.bikeshop.manager.application.service.CobroService;
import com.bikeshop.manager.domain.tenant.Cobro;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cobros")
@RequiredArgsConstructor
public class CobroController {

    private final CobroService cobroService;

    @PostMapping
    public ResponseEntity<Cobro> liquidar(@Valid @RequestBody CobroRequest request) {
        return ResponseEntity.ok(cobroService.liquidar(request));
    }

    @GetMapping
    public ResponseEntity<List<Cobro>> listar() {
        return ResponseEntity.ok(cobroService.listar());
    }
}
