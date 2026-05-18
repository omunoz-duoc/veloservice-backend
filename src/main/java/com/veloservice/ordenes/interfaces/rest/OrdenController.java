package com.veloservice.ordenes.interfaces.rest;
 
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.UsuarioContext;
import com.veloservice.ordenes.application.dto.OrdenMetricasResult;
import com.veloservice.ordenes.application.usecase.ComentarioService;
import com.veloservice.ordenes.application.usecase.MultimediaService;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.interfaces.mapper.MultimediaMapper;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.ordenes.interfaces.mapper.OrdenMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
 
/**
 * REST endpoints for work orders.
 */
@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenController {
 
    private final OrdenService ordenService;
    private final OrdenRepository ordenRepository;
    private final ComentarioService comentarioService;
    private final MultimediaService multimediaService;
 
    /**
     * Creates a new work order.
     */
    @PostMapping
    public ResponseEntity<OrdenResponse> crear(@Valid @RequestBody NuevaOrdenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                OrdenMapper.toResponse(ordenService.crearNuevaOrden(OrdenMapper.toCommand(request)))
        );
    }
 
    /**
     * Lists all work orders for the authenticated mechanic.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<OrdenResponse> ordenes = OrdenMapper.toResponseList(ordenService.listar());
        return ResponseEntity.ok(Map.of(
                "total", ordenes.size(),
                "ordenes", ordenes
        ));
    }
 
    private static final java.util.Set<EstadoOrdenEnum> EN_PROCESO_ESTADOS = EnumSet.of(
        EstadoOrdenEnum.en_diagnostico,
        EstadoOrdenEnum.esperando_repuestos,
        EstadoOrdenEnum.en_reparacion,
        EstadoOrdenEnum.control_calidad
    );

    /**
     * Returns order count grouped by status for the authenticated mechanic.
     */
    @GetMapping("/estados")
    public ResponseEntity<Map<String, Long>> estados() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID mecanicoId = UsuarioContext.getCurrentUser();
        if (sucursalId == null || mecanicoId == null) {
            return ResponseEntity.ok(Map.of());
        }
        Map<String, Long> estados = ordenRepository
                .findAllBySucursalIdAndMecanicoIdOrderByFechaIngresoDesc(sucursalId, mecanicoId)
                .stream()
                .filter(o -> o.getEstado() != EstadoOrdenEnum.cancelada)
                .collect(Collectors.groupingBy(
                    o -> toGrupoEstado(o.getEstado()),
                    Collectors.counting()
                ));
        return ResponseEntity.ok(estados);
    }

    private static String toGrupoEstado(EstadoOrdenEnum estado) {
        if (EN_PROCESO_ESTADOS.contains(estado)) return "en_proceso";
        return estado.name();
    }
 
    /**
     * Lists urgent work orders.
     */
    @GetMapping("/urgentes")
    public ResponseEntity<Map<String, Object>> listarUrgentes() {
        List<OrdenResponse> ordenes = OrdenMapper.toResponseList(ordenService.listarUrgentes());
        return ResponseEntity.ok(Map.of(
                "total", ordenes.size(),
                "ordenes", ordenes
        ));
    }
 
    /**
     * Returns order metrics.
     */
    @GetMapping("/metricas")
    public ResponseEntity<OrdenMetricasResponse> metricas() {
        OrdenMetricasResult result = ordenService.metricas();
        return ResponseEntity.ok(new OrdenMetricasResponse(
                result.getRecibidas(),
                result.getEnProceso(),
                result.getListas(),
                result.getEntregadas()
        ));
    }
 
    /**
     * Lists orders ready for delivery.
     */
    @GetMapping("/lista-entrega")
    public ResponseEntity<List<OrdenListaEntregaResponse>> listarListaEntrega() {
        return ResponseEntity.ok(List.of()); // TODO: implementar listarListaEntrega en OrdenService
    }
 
    /**
     * Retrieves a work order by identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(OrdenMapper.toResponse(ordenService.obtener(id)));
    }
 
    /**
     * Changes the state of a work order.
     */
    @PutMapping("/{id}/estado")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenResponse> cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody EstadoChangeRequest request) {
        return ResponseEntity.ok(OrdenMapper.toResponse(
                ordenService.cambiarEstado(id, OrdenMapper.toEstadoChangeCommand(request))
        ));
    }
 
    /**
     * Agrega un servicio a la orden.
     */
    @PostMapping("/{id}/servicios")
    public ResponseEntity<OrdenResponse> agregarServicio(
            @PathVariable UUID id,
            @Valid @RequestBody OrdenServicioRequest request) {
        return ResponseEntity.ok(OrdenMapper.toResponse(
                ordenService.agregarServicio(id, OrdenMapper.toServicioCommand(request))
        ));
    }
 
    /**
     * Agrega un producto a la orden.
     */
    @PostMapping("/{id}/productos")
    public ResponseEntity<OrdenResponse> agregarProducto(
            @PathVariable UUID id,
            @Valid @RequestBody OrdenProductoRequest request) {
        return ResponseEntity.ok(OrdenMapper.toResponse(
            ordenService.agregarProducto(id, com.veloservice.ordenes.interfaces.mapper.OrdenMapper.toProductoCommand(request))
        ));
    }
 
    /**
     * Exports work orders as CSV.
     */
    @GetMapping("/exportar-csv")
    public ResponseEntity<byte[]> exportarCsv() {
        List<OrdenResponse> ordenes = OrdenMapper.toResponseList(ordenService.listar());
 
        StringBuilder csv = new StringBuilder();
        csv.append("id,tipo,estado,descripcion,mecanico,fecha_ingreso,fecha_estimada,")
           .append("cliente_nombre,cliente_apellido,cliente_telefono,")
           .append("bicicleta_marca,bicicleta_modelo,bicicleta_tipo,bicicleta_color,bicicleta_talla\n");
 
        for (OrdenResponse o : ordenes) {
            csv.append(safe(o.getId())).append(",")
               .append(safe(o.getTipo())).append(",")
               .append(safe(o.getEstado())).append(",")
               .append(safe(o.getDescripcion())).append(",")
               .append(safe(o.getMecanico())).append(",")
               .append(safe(o.getFechaIngreso())).append(",")
               .append(safe(o.getFechaEstimada())).append(",");
 
            if (o.getCliente() != null) {
                csv.append(safe(o.getCliente().getNombre())).append(",")
                   .append(safe(o.getCliente().getApellido())).append(",")
                   .append(safe(o.getCliente().getTelefono())).append(",");
            } else {
                csv.append(",,,");
            }
 
            if (o.getBicicleta() != null) {
                csv.append(safe(o.getBicicleta().getMarca())).append(",")
                   .append(safe(o.getBicicleta().getModelo())).append(",")
                   .append(safe(o.getBicicleta().getTipo())).append(",")
                   .append(safe(o.getBicicleta().getColor())).append(",")
                   .append(safe(o.getBicicleta().getTalla()));
            } else {
                csv.append(",,,,");
            }
            csv.append("\n");
        }
 
        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"ordenes.csv\"")
                .contentType(org.springframework.http.MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
 
    private String safe(Object value) {
        if (value == null) return "";
        String str = value.toString().replace("\"", "\"\"");
        if (str.contains(",") || str.contains("\n") || str.contains("\"")) {
            return "\"" + str + "\"";
        }
        return str;
    }

    /**
     * Lists comments for a work order.
     */
    @GetMapping("/{id}/comentarios")
    public ResponseEntity<Map<String, Object>> listarComentarios(@PathVariable UUID id) {
        var comentarios = comentarioService.listarPorOrden(id).stream()
                .map(c -> ComentarioResponse.builder()
                        .id(c.getId())
                        .autor(c.getAutor())
                        .texto(c.getTexto())
                        .creadoEn(c.getCreadoEn())
                        .build())
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(Map.of("comentarios", comentarios));
    }

    /**
     * Adds a comment to a work order.
     */
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponse> agregarComentario(
            @PathVariable UUID id,
            @Valid @RequestBody ComentarioRequest request) {
        var result = comentarioService.agregar(id, request.getTexto());
        return ResponseEntity.ok(ComentarioResponse.builder()
                .id(result.getId())
                .autor(result.getAutor())
                .texto(result.getTexto())
                .creadoEn(result.getCreadoEn())
                .build());
    }

    /**
     * Lists multimedia for a work order.
     */
    @GetMapping("/{id}/multimedia")
    public ResponseEntity<Map<String, Object>> listarMultimedia(@PathVariable UUID id) {
        var multimedia = multimediaService.listarPorOrden(id).stream()
                .map(m -> MultimediaResponse.builder()
                        .id(m.getId())
                        .ordenId(m.getOrdenId())
                        .usuarioId(m.getUsuarioId())
                        .url(m.getUrl())
                        .tipoArchivo(m.getTipoArchivo())
                        .etapa(m.getEtapa())
                        .descripcion(m.getDescripcion())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("multimedia", multimedia));
    }

    /**
     * Uploads multimedia for a work order.
     */
    @PostMapping("/{id}/multimedia")
    public ResponseEntity<MultimediaResponse> subirMultimedia(
            @PathVariable UUID id,
            @RequestParam String etapa,
            @Valid @RequestBody MultimediaRequest request) {
        var result = multimediaService.subir(id, etapa, MultimediaMapper.toCommand(request));
        return ResponseEntity.ok(MultimediaResponse.builder()
                .id(result.getId())
                .ordenId(result.getOrdenId())
                .usuarioId(result.getUsuarioId())
                .url(result.getUrl())
                .tipoArchivo(result.getTipoArchivo())
                .etapa(result.getEtapa())
                .descripcion(result.getDescripcion())
                .createdAt(result.getCreatedAt())
                .build());
    }

    /**
     * Deletes multimedia from a work order.
     */
    @DeleteMapping("/{id}/multimedia/{mediaId}")
    public ResponseEntity<Map<String, Object>> eliminarMultimedia(
            @PathVariable UUID id,
            @PathVariable UUID mediaId) {
        multimediaService.eliminar(mediaId);
        return ResponseEntity.ok(Map.of());
    }

    /**
     * Lists products for a work order.
     */
    @GetMapping("/{id}/productos")
    public ResponseEntity<Map<String, Object>> listarProductos(@PathVariable UUID id) {
        var productos = ordenService.listarProductosPorOrden(id).stream()
                .map(r -> OrdenProductoResponse.builder()
                        .id(r.getId())
                        .productoId(r.getProductoId())
                        .nombre(r.getNombre())
                        .sku(r.getSku())
                        .cantidad(r.getCantidad())
                        .precioVenta(r.getPrecioVenta())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("productos", productos));
    }
}
 
