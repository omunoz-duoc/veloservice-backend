package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.ProveedorRequest;
import com.bikeshop.manager.application.dto.ProveedorSucursalRequest;
import com.bikeshop.manager.domain.tenant.Proveedor;
import com.bikeshop.manager.domain.tenant.SucursalProveedor;
import com.bikeshop.manager.infrastructure.persistence.repository.ProveedorRepository;
import com.bikeshop.manager.infrastructure.persistence.repository.SucursalProveedorRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Handles supplier operations.
 */
@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final SucursalProveedorRepository sucursalProveedorRepository;

    /**
     * Creates a supplier for the current tenant.
     *
     * @param request supplier request
     * @return created supplier
     */
    @TenantOperation
    @Transactional
    public Proveedor crear(ProveedorRequest request) {
        if (SucursalContext.getCurrentSucursal() == null) {
            throw new IllegalStateException("Contexto de sucursal requerido");
        }
        Proveedor proveedor = Proveedor.builder()
                .nombre(request.getNombre())
                .rut(request.getRut())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .direccion(request.getDireccion())
                .build();
        return proveedorRepository.save(proveedor);
    }

    /**
     * Lists suppliers for the current tenant.
     *
     * @return suppliers
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<Proveedor> listar() {
        return proveedorRepository.findAll();
    }

    /**
     * Assigns a global supplier to the current branch.
     *
     * @param request branch-provider assignment payload
     * @return created link
     */
    @TenantOperation
    @Transactional
    public SucursalProveedor asignarASucursal(ProveedorSucursalRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            throw new IllegalStateException("Contexto de sucursal requerido");
        }

        if (sucursalProveedorRepository.existsBySucursalIdAndProveedorId(sucursalId, request.getProveedorId())) {
            throw new IllegalArgumentException("El proveedor ya está asignado a esta sucursal");
        }

        SucursalProveedor vinculo = SucursalProveedor.builder()
                .sucursalId(sucursalId)
                .proveedorId(request.getProveedorId())
                .codigoCliente(request.getCodigoCliente())
                .condicionPago(request.getCondicionPago())
                .contactoAsignado(request.getContactoAsignado())
                .build();
        return sucursalProveedorRepository.save(vinculo);
    }
}
