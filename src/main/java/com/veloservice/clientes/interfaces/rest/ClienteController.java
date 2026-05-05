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

import com.veloservice.clientes.application.usecase.ClienteService;
import com.veloservice.clientes.interfaces.mapper.ClienteMapper;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for customers.
 */
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    /**
     * Creates a customer for the current tenant.
     *
     * @param request customer request payload
     * @return created customer
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ClienteMapper.toResponse(
                clienteService.crear(ClienteMapper.toCommand(request))
        ));
    }

    /**
     * Lists customers for the current tenant.
     *
     * @return customer list
     */
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(ClienteMapper.toResponseList(clienteService.listar()));
    }

    /**
     * Retrieves a customer by identifier.
     *
     * @param id customer identifier
     * @return customer response
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(ClienteMapper.toResponse(clienteService.obtener(id)));
    }
}