package com.veloservice.ordenes.application.usecase;

import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdenService {

    private final OrdenRepository ordenRepository;

    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenReadResult> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            return ordenRepository.findReadBySucursalId(sucursalId);
        }

        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId != null) {
            return ordenRepository.findReadByTallerId(tallerId);
        }

        throw new IllegalStateException("Contexto de taller o sucursal requerido");
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenReadResult obtener(String id) {
        UUID tallerId = TallerContext.getCurrentTaller();
        UUID sucursalId = SucursalContext.getCurrentSucursal();

        if (sucursalId != null) {
            return buscarEnSucursal(id, sucursalId)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        }

        if (tallerId != null) {
            return buscarEnTaller(id, tallerId)
                    .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));
        }

        throw new IllegalStateException("Contexto de taller o sucursal requerido");
    }

    private Optional<OrdenReadResult> buscarEnTaller(String id, UUID tallerId) {
        Optional<OrdenReadResult> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findReadByIdAndTallerId(uuid, tallerId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findReadByNumeroOrdenAndTallerId(id, tallerId);
    }

    private Optional<OrdenReadResult> buscarEnSucursal(String id, UUID sucursalId) {
        Optional<OrdenReadResult> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findReadByIdAndSucursalId(uuid, sucursalId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findReadByNumeroOrdenAndSucursalId(id, sucursalId);
    }

    private Optional<UUID> parseUuid(String id) {
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
