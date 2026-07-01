package com.veloservice.finanzas.interfaces.rest;

import com.veloservice.finanzas.application.dto.CobroResult;
import com.veloservice.finanzas.application.usecase.CobroService;
import com.veloservice.finanzas.interfaces.mapper.CobroMapper;
import com.veloservice.finanzas.interfaces.rest.dto.CobroRequest;
import com.veloservice.finanzas.interfaces.rest.dto.CobroResponse;
import com.veloservice.finanzas.interfaces.rest.dto.FinanzasHoyResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cobros")
@RequiredArgsConstructor
public class CobroController {

    private final CobroService cobroService;

    @PostMapping
    public ResponseEntity<CobroResponse> liquidar(@Valid @RequestBody CobroRequest request) {
        CobroResult result = cobroService.liquidar(CobroMapper.toCommand(request));
        return ResponseEntity.ok(CobroMapper.toResponse(result));
    }

    @GetMapping
    public ResponseEntity<List<CobroResponse>> listar() {
        return ResponseEntity.ok(CobroMapper.toResponseList(cobroService.listar()));
    }

    @GetMapping("/finanzas/hoy")
    public ResponseEntity<FinanzasHoyResponse> finanzasHoy() {
        return ResponseEntity.ok(CobroMapper.toResponse(cobroService.finanzasHoy()));
    }
}