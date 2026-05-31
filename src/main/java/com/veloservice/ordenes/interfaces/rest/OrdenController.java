package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.ordenes.interfaces.rest.dto.OrdenReadListResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenReadResponse;

import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.application.usecase.OrdenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('mecanico') or hasRole('recepcionista') or hasRole('jefe_taller') or hasRole('admin_taller')")
public class OrdenController {

    private final OrdenService ordenService;

    @GetMapping
    public ResponseEntity<OrdenReadListResponse> listar() {
        List<OrdenReadResponse> ordenes = ordenService.listar().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(new OrdenReadListResponse(ordenes.size(), ordenes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenReadResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(toResponse(ordenService.obtener(id)));
    }

    private OrdenReadResponse toResponse(OrdenReadResult result) {
        OrdenReadResponse.MecanicoResponse mecanico = null;
        if (result.mecanicoId() != null) {
            mecanico = new OrdenReadResponse.MecanicoResponse(
                    result.mecanicoId(),
                    result.mecanicoNombre(),
                    result.mecanicoApellido()
            );
        }

        return new OrdenReadResponse(
                result.id(),
                result.numeroOrden(),
                result.tallerId(),
                result.sucursalId(),
                new OrdenReadResponse.CatalogoResponse(
                        result.estadoId(),
                        result.estadoCodigo(),
                        result.estadoNombre()
                ),
                new OrdenReadResponse.CatalogoResponse(
                        result.tipoId(),
                        result.tipoCodigo(),
                        result.tipoNombre()
                ),
                result.fechaIngreso(),
                result.fechaPrometida(),
                result.fechaEntrega(),
                result.diagnosticoInicial(),
                result.diagnosticoFinal(),
                result.observacionesCliente(),
                new OrdenReadResponse.BicicletaResponse(
                        result.bicicletaId(),
                        result.bicicletaMarca(),
                        result.bicicletaModelo(),
                        result.bicicletaTipo(),
                        result.bicicletaAro(),
                        result.bicicletaColor(),
                        result.bicicletaNumeroSerie()
                ),
                new OrdenReadResponse.ClienteResponse(
                        result.clienteId(),
                        result.clienteNombre(),
                        result.clienteApellido(),
                        result.clienteTelefono(),
                        result.clienteEmail(),
                        result.clienteRut()
                ),
                mecanico
        );
    }
}
