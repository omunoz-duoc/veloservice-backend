package com.veloservice.administracion.application.usecase;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.veloservice.administracion.application.dto.NuevaSucursalCommand;
import com.veloservice.administracion.application.dto.SucursalResult;
import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.TenantOperation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SucursalService {
    private final SucursalRepository sucursalRepository;

    /**
     * Lista todas las sucursales asociadas al taller actual. Si no hay un taller activo, devuelve una lista vacía.
     * @return
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<SucursalResult> listar() {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId != null) {
            return sucursalRepository.findAllByTallerId(tallerId).stream()
                    .map(this::toResult)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private SucursalResult toResult(Sucursal sucursal) {
        SucursalResult.SucursalResultBuilder builder = SucursalResult.builder()
                .id(sucursal.getId())
                .nombre(sucursal.getNombre());
        return builder.build();
    }

    /**
     * Crea una nueva sucursal asociada al taller actual. Si no hay un taller activo, lanza una excepción.
     * @param command
     * @return
     */
    @TenantOperation
    @Transactional
    public SucursalResult crear(NuevaSucursalCommand command) {
        UUID tallerId = TallerContext.getCurrentTaller();

        if (tallerId == null) {
            throw new IllegalStateException("No se puede crear una sucursal sin un taller activo");
        }

        Sucursal sucursal = Sucursal.builder()
                .tallerId(tallerId)
                .nombre(command.getNombre())
                .direccion(command.getDireccion())
                .telefono(command.getTelefono())
                .email(command.getEmail())
                .activo(command.isActivo())
                .build();

        sucursalRepository.save(sucursal);
        return toResult(sucursal);
    }
}
