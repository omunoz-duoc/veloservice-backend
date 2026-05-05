package com.veloservice.clientes.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.clientes.application.usecase.BicicletaService;
import com.veloservice.clientes.interfaces.mapper.BicicletaMapper;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for bikes.
 */
@RestController
@RequestMapping("/bicicletas")
@RequiredArgsConstructor
public class BicicletaController {

    private final BicicletaService bicicletaService;

    /**
     * Creates a bike for a customer.
     *
     * @param clienteId customer identifier
     * @param request bike request payload
     * @return created bike
     */
    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<BicicletaResponse> crear(
            @PathVariable UUID clienteId,
            @Valid @RequestBody BicicletaRequest request) {
        return ResponseEntity.ok(BicicletaMapper.toResponse(
            bicicletaService.crear(clienteId, BicicletaMapper.toCommand(request))
        ));
    }

    /**
     * Lists bikes for a customer.
     *
     * @param clienteId customer identifier
     * @return customer bikes
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<BicicletaResponse>> listarPorCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(BicicletaMapper.toResponseList(
                bicicletaService.listarPorCliente(clienteId)
        ));
    }

    /**
     * Lists all bikes for the current tenant.
     *
     * @return tenant bikes
     */
    @GetMapping
    public ResponseEntity<List<BicicletaResponse>> listarTodas() {
        return ResponseEntity.ok(BicicletaMapper.toResponseList(
                bicicletaService.listarTodas()
        ));
    }
}