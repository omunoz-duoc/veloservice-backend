package com.bikeshop.manager.application.service;

import com.bikeshop.manager.application.dto.ProveedorRequest;
import com.bikeshop.manager.domain.tenant.Proveedor;
import com.bikeshop.manager.infrastructure.persistence.repository.ProveedorRepository;
import com.bikeshop.manager.infrastructure.rls.TenantOperation;
import com.bikeshop.manager.infrastructure.security.TenantContext;
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

    /**
     * Creates a supplier for the current tenant.
     *
     * @param request supplier request
     * @return created supplier
     */
    @TenantOperation
    @Transactional
    public Proveedor crear(ProveedorRequest request) {
        UUID tallerId = TenantContext.getCurrentTenant();
        if (tallerId == null) {
            throw new IllegalStateException("Contexto de taller requerido");
        }
        Proveedor proveedor = Proveedor.builder()
                .tallerId(tallerId)
                .nombre(request.getNombre())
                .rut(request.getRut())
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .direccion(request.getDireccion())
                .condicionPago(request.getCondicionPago())
                .contactoAsignado(request.getContactoAsignado())
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
        UUID tallerId = TenantContext.getCurrentTenant();
        if (tallerId == null) {
            return List.of();
        }
        return proveedorRepository.findAllByTallerId(tallerId);
    }
}
