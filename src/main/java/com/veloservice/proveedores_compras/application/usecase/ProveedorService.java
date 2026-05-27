package com.veloservice.proveedores_compras.application.usecase;

import com.veloservice.proveedores_compras.application.dto.ProveedorCreateCommand;
import com.veloservice.proveedores_compras.application.dto.ProveedorResult;
import com.veloservice.proveedores_compras.application.dto.SucursalProveedorCreateCommand;
import com.veloservice.proveedores_compras.application.dto.SucursalProveedorResult;
import com.veloservice.proveedores_compras.domain.model.Proveedor;
import com.veloservice.proveedores_compras.domain.model.SucursalProveedor;
import com.veloservice.proveedores_compras.infraestructure.persistence.repository.ProveedorRepository;
import com.veloservice.proveedores_compras.infraestructure.persistence.repository.SucursalProveedorRepository;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.config.tenant.SucursalContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public ProveedorResult crear(ProveedorCreateCommand command) {
        if (SucursalContext.getCurrentSucursal() == null) {
            throw new IllegalStateException("Contexto de sucursal requerido");
        }
        Proveedor proveedor = Proveedor.builder()
                .nombre(command.getNombre())
                .rut(command.getRut())
                .telefono(command.getTelefono())
                .email(command.getEmail())
                .direccion(command.getDireccion())
                .build();
        return toResult(proveedorRepository.save(proveedor));
    }

    /**
     * Lists suppliers for the current tenant.
     *
     * @return suppliers
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<ProveedorResult> listar() {
        return proveedorRepository.findAll().stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    /**
     * Assigns a global supplier to the current branch.
     *
     * @param request branch-provider assignment payload
     * @return created link
     */
    @TenantOperation
    @Transactional
    public SucursalProveedorResult asignarASucursal(SucursalProveedorCreateCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            throw new IllegalStateException("Contexto de sucursal requerido");
        }

        if (sucursalProveedorRepository.existsBySucursalIdAndProveedorId(sucursalId, command.getProveedorId())) {
            throw new IllegalArgumentException("El proveedor ya está asignado a esta sucursal");
        }

        SucursalProveedor vinculo = SucursalProveedor.builder()
                .sucursalId(sucursalId)
                .proveedorId(command.getProveedorId())
                .codigoCliente(command.getCodigoCliente())
                .condicionPago(command.getCondicionPago())
                .contactoAsignado(command.getContactoAsignado())
                .build();
        return toResult(sucursalProveedorRepository.save(vinculo));
    }

    private ProveedorResult toResult(Proveedor proveedor) {
        return ProveedorResult.builder()
                .id(proveedor.getId())
                .nombre(proveedor.getNombre())
                .rut(proveedor.getRut())
                .telefono(proveedor.getTelefono())
                .email(proveedor.getEmail())
                .direccion(proveedor.getDireccion())
                .activo(proveedor.getActivo())
                .createdAt(proveedor.getCreatedAt())
                .build();
    }

    private SucursalProveedorResult toResult(SucursalProveedor vinculo) {
        return SucursalProveedorResult.builder()
                .id(vinculo.getId())
                .sucursalId(vinculo.getSucursalId())
                .proveedorId(vinculo.getProveedorId())
                .codigoCliente(vinculo.getCodigoCliente())
                .condicionPago(vinculo.getCondicionPago())
                .contactoAsignado(vinculo.getContactoAsignado())
                .activo(vinculo.getActivo())
                .createdAt(vinculo.getCreatedAt())
                .build();
    }
}