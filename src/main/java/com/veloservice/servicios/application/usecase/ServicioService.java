package com.veloservice.servicios.application.usecase;

import com.veloservice.servicios.application.dto.ServicioCreateCommand;
import com.veloservice.servicios.application.dto.ServicioResult;
import com.veloservice.servicios.application.dto.SucursalServicioPrecioCommand;
import com.veloservice.servicios.application.dto.SucursalServicioResult;
import com.veloservice.servicios.domain.model.Servicio;
import com.veloservice.servicios.domain.model.SucursalServicio;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.SucursalServicioRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.administracion.domain.model.Sucursal;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final SucursalServicioRepository sucursalServicioRepository;
    private final SucursalRepository sucursalRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public ServicioService(
            ServicioRepository servicioRepository,
            SucursalServicioRepository sucursalServicioRepository,
            SucursalRepository sucursalRepository
    ) {
        this.servicioRepository = servicioRepository;
        this.sucursalServicioRepository = sucursalServicioRepository;
        this.sucursalRepository = sucursalRepository;
    }

    public ServicioService(
            ServicioRepository servicioRepository,
            SucursalServicioRepository sucursalServicioRepository
    ) {
        this(servicioRepository, sucursalServicioRepository, null);
    }

    @Transactional(readOnly = true)
        public List<ServicioResult> listar() {
        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId == null) {
            throw new IllegalStateException("Taller no presente en contexto");
        }
        return servicioRepository.findByTallerIdAndActivoTrueOrderByNombreAsc(tallerId).stream()
            .map(this::toResult)
            .collect(Collectors.toList());
    }

    @Transactional
        public ServicioResult crear(ServicioCreateCommand command) {
        Servicio s = Servicio.builder()
            .nombre(command.getNombre())
            .descripcion(command.getDescripcion())
            .precioBase(command.getPrecioBase())
            .activo(command.getActivo() == null ? true : command.getActivo())
                .build();
        s = servicioRepository.save(s);
        return toResult(s);
    }

    @Transactional
    public ServicioResult actualizar(UUID id, ServicioCreateCommand command) {
        Servicio s = servicioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        s.setNombre(command.getNombre());
        s.setDescripcion(command.getDescripcion());
        s.setPrecioBase(command.getPrecioBase());
        s.setActivo(command.getActivo() == null ? s.getActivo() : command.getActivo());
        s = servicioRepository.save(s);
        return toResult(s);
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!servicioRepository.existsById(id)) throw new IllegalArgumentException("Servicio no encontrado");
        servicioRepository.deleteById(id);
    }

    @Transactional
    public void asignarPrecioSucursal(SucursalServicioPrecioCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) throw new IllegalStateException("Sucursal no presente en contexto");

        SucursalServicio link = sucursalServicioRepository.findBySucursalIdAndServicioId(sucursalId, command.getServicioId())
                .orElseGet(() -> SucursalServicio.builder()
                        .sucursalId(sucursalId)
                        .servicioId(command.getServicioId())
                        .build());
        link.setPrecioPersonalizado(command.getPrecioPersonalizado());
        link.setActivo(true);
        sucursalServicioRepository.save(link);
    }

    @Transactional(readOnly = true)
    public List<SucursalServicioResult> listarServiciosSucursal() {
        return listarServiciosSucursal(null);
    }

    @Transactional(readOnly = true)
    public List<SucursalServicioResult> listarServiciosSucursal(UUID requestedSucursalId) {
        UUID sucursalId = resolverSucursal(requestedSucursalId);
        if (sucursalId == null) {
            throw new IllegalArgumentException("Sucursal requerida");
        }
        return sucursalServicioRepository.findAllBySucursalIdAndActivoTrue(sucursalId).stream()
                .filter(link -> servicioRepository.findById(link.getServicioId())
                        .map(Servicio::getActivo)
                        .orElse(false))
                .map(this::toSucursalResult)
                .collect(Collectors.toList());
    }

    private ServicioResult toResult(Servicio servicio) {
        return ServicioResult.builder()
                .id(servicio.getId())
                .nombre(servicio.getNombre())
                .descripcion(servicio.getDescripcion())
                .precioBase(servicio.getPrecioBase())
                .activo(servicio.getActivo())
                .build();
    }

    private SucursalServicioResult toSucursalResult(SucursalServicio link) {
        Servicio servicio = servicioRepository.findById(link.getServicioId())
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .orElseThrow(() -> new IllegalStateException("Servicio activo no encontrado"));
        java.math.BigDecimal precioVigente = link.getPrecioPersonalizado() != null
                ? link.getPrecioPersonalizado()
                : servicio.getPrecioBase();
        return SucursalServicioResult.builder()
                .id(link.getId())
                .sucursalId(link.getSucursalId())
                .servicioId(link.getServicioId())
                .nombre(servicio.getNombre())
                .descripcion(servicio.getDescripcion())
                .precioBase(servicio.getPrecioBase())
                .precioPersonalizado(link.getPrecioPersonalizado())
                .precioVigente(precioVigente)
                .activo(link.getActivo())
                .createdAt(link.getCreatedAt())
                .build();
    }

    private UUID resolverSucursal(UUID requestedSucursalId) {
        UUID scopedSucursalId = SucursalContext.getCurrentSucursal();
        if (scopedSucursalId != null) {
            if (requestedSucursalId != null && !scopedSucursalId.equals(requestedSucursalId)) {
                throw new AccessDeniedException("Sucursal fuera del alcance autorizado");
            }
            return scopedSucursalId;
        }
        UUID tallerId = TallerContext.getCurrentTaller();
        if (requestedSucursalId != null) {
            if (tallerId == null || !sucursalRepository.existsByIdAndTallerId(requestedSucursalId, tallerId)) {
                throw new AccessDeniedException("Sucursal fuera del alcance autorizado");
            }
            return requestedSucursalId;
        }
        return tallerId == null || sucursalRepository == null ? null : sucursalRepository
                .findFirstByTallerIdAndActivoTrueOrderByCreatedAtAsc(tallerId)
                .map(Sucursal::getId)
                .orElse(null);
    }
}
