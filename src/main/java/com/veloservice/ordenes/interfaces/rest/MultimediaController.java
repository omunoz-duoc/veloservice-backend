package com.veloservice.ordenes.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.interfaces.mapper.MultimediaMapper;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/multimedia")
@RequiredArgsConstructor
public class MultimediaController {

    private final MultimediaService multimediaService;

    @PostMapping("/orden/{ordenId}")
    public ResponseEntity<MultimediaResponse> subir(
            @PathVariable UUID ordenId,
            @RequestParam String etapa,
            @Valid @RequestBody MultimediaRequest request) {
        return ResponseEntity.ok(MultimediaMapper.toResponse(
                multimediaService.subir(ordenId, etapa, MultimediaMapper.toCommand(request))
        ));
    }

    @GetMapping("/orden/{ordenId}")
    public ResponseEntity<List<MultimediaResponse>> listarPorOrden(@PathVariable UUID ordenId) {
        return ResponseEntity.ok(MultimediaMapper.toResponseList(multimediaService.listarPorOrden(ordenId)));
    }
}