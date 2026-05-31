package com.veloservice.clientes.interfaces.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.clientes.application.usecase.BicicletaService;
import com.veloservice.clientes.interfaces.mapper.BicicletaMapper;
import com.veloservice.clientes.interfaces.rest.dto.BicicletaRequest;
import com.veloservice.clientes.interfaces.rest.dto.BicicletaResponse;
import com.veloservice.clientes.interfaces.rest.dto.HojaVidaBicicletaResponse;

import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenProducto;
import com.veloservice.ordenes.domain.model.Multimedia;
import java.util.stream.Collectors;
import java.util.Optional;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for bikes.
 */
@RestController
@RequestMapping("/bicicletas")
@RequiredArgsConstructor
public class BicicletaController {

    private final BicicletaService bicicletaService;

    private final OrdenRepository ordenRepository;
    private final OrdenProductoRepository ordenProductoRepository;
    private final MultimediaRepository multimediaRepository;

    /**
     * Creates a bike for a customer.
     *
     * @param clienteId customer identifier
     * @param request bike request payload
     * @return created bike
     */
    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<BicicletaResponse> crear(
            @PathVariable UUID clienteId,
            @Valid @RequestBody BicicletaRequest request) {
        return ResponseEntity.ok(BicicletaMapper.toResponse(
            bicicletaService.crear(clienteId, BicicletaMapper.toCommand(request))
        ));
    }

    /**
     * Lists bikes for a customer.
     *
     * @param clienteId customer identifier
     * @return customer bikes
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<BicicletaResponse>> listarPorCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(BicicletaMapper.toResponseList(
                bicicletaService.listarPorCliente(clienteId)
        ));
    }

    /**
     * Lists all bikes for the current tenant.
     *
     * @return tenant bikes
     */
    @GetMapping
    public ResponseEntity<List<BicicletaResponse>> listarTodas() {
        return ResponseEntity.ok(BicicletaMapper.toResponseList(
                bicicletaService.listarTodas()
        ));
    }

        /**
         * Hoja de vida consolidada de la bicicleta: historial, repuestos y multimedia
         */
        @GetMapping("/{id}/hoja-vida")
        public ResponseEntity<HojaVidaBicicletaResponse> hojaVida(@PathVariable UUID id) {
        // Buscar bicicleta
        var biciOpt = bicicletaService.listarTodas().stream().filter(b -> b.getId().equals(id)).findFirst();
        if (biciOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var bici = biciOpt.get();

        // Historial de órdenes
        var ordenes = ordenRepository.findAll().stream()
            .filter(o -> id.equals(o.getBicicletaId()))
            .collect(Collectors.toList());

        var servicios = ordenes.stream().map(o -> HojaVidaBicicletaResponse.ServicioDTO.builder()
            .ordenId(o.getId())
            .descripcion(o.getDescripcionTrabajo())
            .fechaIngreso(o.getFechaIngreso())
            .fechaEntrega(o.getFechaEntrega())
            .estado(o.getEstado() != null ? o.getEstado().name() : null)
            .build()).collect(Collectors.toList());

        // Repuestos usados
        var repuestos = ordenes.stream()
            .flatMap(o -> ordenProductoRepository.findByOrdenId(o.getId()).stream())
            .map(op -> HojaVidaBicicletaResponse.RepuestoDTO.builder()
                .productoId(op.getProductoId())
                .nombre(op.getNotas())
                .cantidad(op.getCantidad() != null ? op.getCantidad() : 0)
                .notas(op.getNotas())
                .build())
            .collect(Collectors.toList());

        // Multimedia asociada
        var multimedia = ordenes.stream()
            .flatMap(o -> multimediaRepository.findByOrdenId(o.getId()).stream())
            .map(m -> HojaVidaBicicletaResponse.MultimediaDTO.builder()
                .id(m.getId())
                .url(m.getUrl())
                .tipoArchivo(m.getTipoArchivo() != null ? m.getTipoArchivo().name() : null)
                .descripcion(m.getDescripcion())
                .createdAt(m.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        var response = HojaVidaBicicletaResponse.builder()
            .bicicletaId(bici.getId())
            .marca(bici.getMarca())
            .modelo(bici.getModelo())
            .color(bici.getColor())
            .tipo(bici.getTipo())
            .numeroSerie(bici.getNumeroSerie())
            .servicios(servicios)
            .repuestos(repuestos)
            .multimedia(multimedia)
            .build();
        return ResponseEntity.ok(response);
        }
}