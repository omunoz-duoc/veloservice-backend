package com.veloservice.proveedores_compras.interfaces.rest;

import com.veloservice.proveedores_compras.interfaces.rest.dto.ProveedorRequest;
import com.veloservice.proveedores_compras.interfaces.rest.dto.ProveedorResponse;
import com.veloservice.proveedores_compras.interfaces.rest.dto.ProveedorSucursalRequest;
import com.veloservice.proveedores_compras.interfaces.rest.dto.SucursalProveedorResponse;

import com.veloservice.proveedores_compras.application.usecase.ProveedorService;
import com.veloservice.proveedores_compras.interfaces.mapper.ProveedorMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for suppliers.
 */
@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    /**
     * Creates a supplier.
     *
     * @param request supplier request
     * @return created supplier
     */
    @PostMapping
    public ResponseEntity<ProveedorResponse> crear(@Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(ProveedorMapper.toResponse(
                proveedorService.crear(ProveedorMapper.toCommand(request))
        ));
    }

    /**
     * Lists suppliers for the current tenant.
     *
     * @return suppliers
     */
    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listar() {
        return ResponseEntity.ok(ProveedorMapper.toResponseList(proveedorService.listar()));
    }

    /**
     * Assigns a supplier to the current branch.
     *
     * @param request assignment payload
     * @return created branch-supplier link
     */
    @PostMapping("/asignar")
    public ResponseEntity<SucursalProveedorResponse> asignar(@Valid @RequestBody ProveedorSucursalRequest request) {
        return ResponseEntity.ok(ProveedorMapper.toResponse(
                proveedorService.asignarASucursal(ProveedorMapper.toSucursalCommand(request))
        ));
    }
}