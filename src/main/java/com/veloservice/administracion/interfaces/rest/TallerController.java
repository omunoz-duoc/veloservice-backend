package com.veloservice.administracion.interfaces.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.administracion.application.dto.NuevoTallerCommand;
import com.veloservice.administracion.application.usecase.TallerService;
import com.veloservice.administracion.interfaces.mapper.TallerMapper;
import com.veloservice.administracion.interfaces.rest.dto.TallerResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/talleres")
@RequiredArgsConstructor
@PreAuthorize("hasRole('plataforma')")
public class TallerController {

    private final TallerService tallerService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<TallerResponse> talleres = TallerMapper.toResponseList(tallerService.listar());
        return ResponseEntity.ok(Map.of("talleres", talleres));
    }

    @PostMapping
    public ResponseEntity<TallerResponse> crear(@RequestBody NuevoTallerCommand command) {
        TallerResponse response = TallerMapper.toResponse(tallerService.crear(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
