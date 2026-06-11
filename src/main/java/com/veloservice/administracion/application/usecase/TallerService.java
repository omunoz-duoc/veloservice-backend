package com.veloservice.administracion.application.usecase;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.veloservice.administracion.application.dto.NuevoTallerCommand;
import com.veloservice.administracion.application.dto.TallerResult;
import com.veloservice.administracion.domain.model.Taller;
import com.veloservice.administracion.infraestructure.persistence.repository.TallerRepository;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.shared.application.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TallerService {

    private final TallerRepository tallerRepository;

    /**
     * Lista todos los talleres registrados en el sistema.
     * @return
     */
    @Transactional(readOnly = true)
    public List<TallerResult> listar() {
        return tallerRepository.findAll().stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo taller en el sistema. Verifica que no exista otro taller con el mismo RUT antes de crear.
     * @param command
     * @return
     */
    @Transactional
    public TallerResult crear(NuevoTallerCommand command) {
        if (tallerRepository.findByRut(command.getRut()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un taller con el RUT: " + command.getRut());
        }
        OffsetDateTime now = OffsetDateTime.now();
        Taller taller = Taller.builder()
                .rut(command.getRut())
                .nombre(command.getNombre())
                .telefono(command.getTelefono())
                .email(command.getEmail())
                .logoUrl(command.getLogoUrl())
                .planId(command.getPlanId())
                .activo(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toResult(tallerRepository.save(taller));
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public TallerResult obtenerActual() {
        return toResult(obtenerTallerActual());
    }

    @TenantOperation
    @Transactional
    public TallerResult actualizarActual(String nombre, String rut, String telefono, String email, String logoUrl) {
        Taller taller = obtenerTallerActual();
        taller.setNombre(nombre);
        taller.setRut(rut);
        taller.setTelefono(telefono);
        taller.setEmail(email);
        taller.setLogoUrl(logoUrl);
        taller.setUpdatedAt(OffsetDateTime.now());
        return toResult(tallerRepository.save(taller));
    }

    private Taller obtenerTallerActual() {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new ResourceNotFoundException("Taller no encontrado");
        }
        return tallerRepository.findById(tallerId)
                .orElseThrow(() -> new ResourceNotFoundException("Taller no encontrado"));
    }

    private TallerResult toResult(Taller taller) {
        return TallerResult.builder()
                .id(taller.getId())
                .rut(taller.getRut())
                .nombre(taller.getNombre())
                .telefono(taller.getTelefono())
                .email(taller.getEmail())
                .logoUrl(taller.getLogoUrl())
                .planId(taller.getPlanId())
                .activo(Boolean.TRUE.equals(taller.getActivo()))
                .build();
    }
}
