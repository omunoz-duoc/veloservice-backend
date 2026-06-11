package com.veloservice.auth.application.usecase;

import com.veloservice.auth.application.dto.MecanicoResult;
import com.veloservice.auth.domain.model.Usuario;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MecanicoService {

    private static final List<String> ESTADOS_ACTIVOS = List.of(
            EstadoOrdenEnum.recibida.name(),
            EstadoOrdenEnum.en_diagnostico.name(),
            EstadoOrdenEnum.esperando_repuestos.name(),
            EstadoOrdenEnum.en_reparacion.name(),
            EstadoOrdenEnum.control_calidad.name(),
            EstadoOrdenEnum.lista_para_entrega.name()
    );

    private final UsuarioRepository usuarioRepository;
    private final OrdenRepository ordenRepository;

    @Transactional(readOnly = true)
    public List<MecanicoResult> listarActivos() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            return usuarioRepository.findActiveMecanicosBySucursalId(sucursalId).stream()
                    .map(usuario -> toResult(
                            usuario,
                            ordenRepository.countActivasByMecanicoIdAndSucursalId(
                                    usuario.getId(),
                                    sucursalId,
                                    ESTADOS_ACTIVOS
                            )
                    ))
                    .toList();
        }

        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId != null) {
            return usuarioRepository.findActiveMecanicosByTallerId(tallerId).stream()
                    .map(usuario -> toResult(
                            usuario,
                            ordenRepository.countActivasByMecanicoIdAndTallerId(
                                    usuario.getId(),
                                    tallerId,
                                    ESTADOS_ACTIVOS
                            )
                    ))
                    .toList();
        }

        return List.of();
    }

    private MecanicoResult toResult(Usuario usuario, long ordenesActivas) {
        return new MecanicoResult(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                ordenesActivas
        );
    }
}
