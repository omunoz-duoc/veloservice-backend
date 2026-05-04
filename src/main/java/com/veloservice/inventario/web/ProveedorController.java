package com.veloservice.inventario.web;

import com.veloservice.inventario.api.ProveedorRequest;
import com.veloservice.inventario.api.ProveedorSucursalRequest;
import com.veloservice.inventario.internal.service.ProveedorService;
import com.veloservice.inventario.internal.entity.Proveedor;
import com.veloservice.inventario.internal.entity.SucursalProveedor;
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
    public ResponseEntity<Proveedor> crear(@Valid @RequestBody ProveedorRequest request) {
        return ResponseEntity.ok(proveedorService.crear(request));
    }

    /**
     * Lists suppliers for the current tenant.
     *
     * @return suppliers
     */
    @GetMapping
    public ResponseEntity<List<Proveedor>> listar() {
        return ResponseEntity.ok(proveedorService.listar());
    }

    /**
     * Assigns a supplier to the current branch.
     *
     * @param request assignment payload
     * @return created branch-supplier link
     */
    @PostMapping("/asignar")
    public ResponseEntity<SucursalProveedor> asignar(@Valid @RequestBody ProveedorSucursalRequest request) {
        return ResponseEntity.ok(proveedorService.asignarASucursal(request));
    }
}