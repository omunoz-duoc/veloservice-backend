package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenCreateResult;
import com.veloservice.ordenes.application.dto.OrdenCatalogoResult;
import com.veloservice.ordenes.domain.model.TipoOrden;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenDetalleResult;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.OrdenProductoUpdateCommand;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.application.dto.OrdenServicioUpdateCommand;
import com.veloservice.ordenes.application.dto.OrdenUpdateCommand;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.MultimediaConfirmationResult;
import com.veloservice.ordenes.application.dto.MultimediaPresignResult;
import com.veloservice.ordenes.application.dto.OrdenMetricasResult;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenCreateRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenCreateResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenCatalogoResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenDetalleResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenEstadoChangeRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenProductoAddRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenProductoCambioRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenProductoResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenProductoUpdateRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenReadListResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenReadResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenResumenListResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenResumenResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenServicioAddRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenServicioCambioRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenServicioResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenUpdateRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenComentarioRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenComentarioResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenMultimediaResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenMetricasResponse;
import com.veloservice.ordenes.interfaces.rest.dto.MultimediaConfirmRequest;
import com.veloservice.ordenes.interfaces.rest.dto.MultimediaPresignRequest;
import com.veloservice.ordenes.interfaces.rest.dto.MultimediaPresignResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('mecanico') or hasRole('recepcionista') or hasRole('jefe_taller') or hasRole('admin_taller')")
public class OrdenController {

    private final OrdenService ordenService;

    /**
     * Crea una nueva orden de trabajo. El request puede incluir un cliente existente (clienteId) o datos para crear un nuevo cliente (clienteNuevo), y lo mismo para la bicicleta. Si se proporcionan ambos (clienteId y clienteNuevo),
     *  se ignorará clienteNuevo y se asociará la orden al cliente existente. Lo mismo aplica para bicicletaId y bicicletaNuevo.
     * @param request
     * @return
     */
    public record TipoOrdenResponse(String id, String codigo, String nombre) {}

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoOrdenResponse>> listarTipos() {
        List<TipoOrdenResponse> tipos = ordenService.listarTipos().stream()
                .map(t -> new TipoOrdenResponse(t.getId().toString(), t.getCodigo(), t.getNombre()))
                .toList();
        return ResponseEntity.ok(tipos);
    }

    @PostMapping
    public ResponseEntity<OrdenCreateResponse> crear(@Valid @RequestBody OrdenCreateRequest request) {
        OrdenCreateCommand command = toCommand(request);
        OrdenCreateResult result = ordenService.crear(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrdenCreateResponse(result.id(), result.numeroOrden()));
    }

    /**
     * TODO: paginar y/o permitir filtros.
     * Lista todas las órdenes de trabajo con detalles completos. Este endpoint puede ser costoso en términos de rendimiento si hay muchas órdenes, por lo que se recomienda usarlo con paginación o filtros en un entorno real.
     *  Para obtener listados más ligeros, se pueden usar otros endpoints como /ordenes/resumen.
     * @return
     */
    @GetMapping
    public ResponseEntity<OrdenReadListResponse> listar(@RequestParam(required = false) UUID sucursalId) {
        List<OrdenReadResponse> ordenes = ordenService.listar(sucursalId).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(new OrdenReadListResponse(ordenes.size(), ordenes));
    }

    @GetMapping("/urgentes")
    public ResponseEntity<OrdenReadListResponse> listarUrgentes(@RequestParam(required = false) UUID sucursalId) {
        List<OrdenReadResponse> ordenes = ordenService.listar(sucursalId).stream()
                .filter(orden -> isPrioridadUrgente(orden.prioridad()))
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(new OrdenReadListResponse(ordenes.size(), ordenes));
    }

    @GetMapping("/metricas")
    public ResponseEntity<OrdenMetricasResponse> metricas(@RequestParam(required = false) UUID sucursalId) {
        OrdenMetricasResult result = ordenService.metricas(sucursalId);
        return ResponseEntity.ok(new OrdenMetricasResponse(
                result.recibidas(), result.enProceso(), result.listas(), result.entregadas()
        ));
    }

    @GetMapping("/catalogos/estados")
    public ResponseEntity<List<OrdenCatalogoResponse>> listarEstadosCatalogo() {
        return ResponseEntity.ok(ordenService.listarEstadosCatalogo().stream()
                .map(this::toCatalogoResponse)
                .toList());
    }

    @GetMapping("/catalogos/tipos")
    public ResponseEntity<List<OrdenCatalogoResponse>> listarTiposCatalogo() {
        return ResponseEntity.ok(ordenService.listarTiposCatalogo().stream()
                .map(this::toCatalogoResponse)
                .toList());
    }

    @GetMapping("/catalogos/prioridades")
    public ResponseEntity<List<OrdenCatalogoResponse>> listarPrioridadesCatalogo() {
        return ResponseEntity.ok(ordenService.listarPrioridadesCatalogo().stream()
                .map(this::toCatalogoResponse)
                .toList());
    }

    /**
     * TODO: paginar y/o permitir filtros.
     * Lista todas las órdenes de trabajo con un resumen de información (número de orden, tipo, fecha de ingreso, nombre del cliente, marca/modelo de la bicicleta, diagnóstico inicial, estado y prioridad).
     * Este endpoint es ideal para vistas de listados donde no se necesitan todos los detalles de la orden, sino solo información clave para identificarla y su estado general.
     * @return
     */
    @GetMapping("/resumen")
    public ResponseEntity<OrdenResumenListResponse> listarResumen() {
        List<OrdenReadResult> results = ordenService.listar();
        List<OrdenResumenResponse> resumen = results.stream()
            .map(this::toResumenResponse)
            .toList();
        return ResponseEntity.ok(new OrdenResumenListResponse(resumen.size(), resumen));
    }

    /**
     * Obtiene los detalles completos de una orden de trabajo por su ID. Incluye toda la información de la orden, cliente, bicicleta, mecánico asignado, comentarios y multimedia asociada.
     * @param id
     * @return
     */
    @GetMapping({
            "/{id:[0-9a-fA-F-]{36}}",
            "/{id:[Oo][Tt]-[A-Za-z0-9-]{1,40}}",
            "/{id:[Aa][Pp]-[A-Za-z0-9-]{1,40}}"
    })
    public ResponseEntity<OrdenDetalleResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(toDetalleResponse(ordenService.obtenerDetalle(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrdenDetalleResponse> actualizar(@PathVariable String id,
                                                           @Valid @RequestBody OrdenUpdateRequest request) {
        List<OrdenProductoAddCommand> productosAgregar = request.getProductosAgregar() != null
                ? request.getProductosAgregar().stream()
                    .map(this::toProductoCommand)
                    .collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();
        List<OrdenProductoUpdateCommand> productosActualizar = request.getProductosActualizar() != null
                ? request.getProductosActualizar().stream()
                    .map(this::toProductoUpdateCommand)
                    .collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>();
        List<UUID> productosEliminar = request.getProductosEliminar() != null
                ? new ArrayList<>(request.getProductosEliminar())
                : new ArrayList<>();
        List<OrdenServicioAddCommand> serviciosAgregar = new ArrayList<>();
        List<OrdenServicioUpdateCommand> serviciosActualizar = new ArrayList<>();
        List<UUID> serviciosEliminar = new ArrayList<>();

        mapProductoCambios(request.getProductosCambios(), productosAgregar, productosActualizar, productosEliminar);
        mapProductoCambios(request.getProductos(), productosAgregar, productosActualizar, productosEliminar);
        mapServicioCambios(request.getServiciosCambios(), serviciosAgregar, serviciosActualizar, serviciosEliminar);

        OrdenUpdateCommand command = new OrdenUpdateCommand(
                request.getEstadoCodigo(),
                request.getEstadoObservacion(),
                request.getTipoCodigo(),
                request.getPrioridad(),
                request.getMecanicoId(),
                productosAgregar.isEmpty() ? null : productosAgregar,
                productosActualizar.isEmpty() ? null : productosActualizar,
                productosEliminar.isEmpty() ? null : productosEliminar,
                serviciosAgregar.isEmpty() ? null : serviciosAgregar,
                serviciosActualizar.isEmpty() ? null : serviciosActualizar,
                serviciosEliminar.isEmpty() ? null : serviciosEliminar
        );
        return ResponseEntity.ok(toDetalleResponse(ordenService.actualizar(id, command)));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenDetalleResponse> cambiarEstado(@PathVariable String id,
                                                              @Valid @RequestBody OrdenEstadoChangeRequest request) {
        ordenService.cambiarEstado(id, new OrdenEstadoChangeCommand(request.getCodigo(), request.getObservacion()));
        return ResponseEntity.ok(toDetalleResponse(ordenService.obtenerDetalle(id)));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<OrdenComentarioResponse> agregarComentario(
            @PathVariable String id,
            @Valid @RequestBody OrdenComentarioRequest request
    ) {
        ComentarioResult result = ordenService.agregarComentario(id, request.texto());
        return ResponseEntity.status(HttpStatus.CREATED).body(toComentarioResponse(result));
    }

    @PostMapping(value = "/{id}/multimedia", consumes = "multipart/form-data")
    public ResponseEntity<OrdenMultimediaResponse> agregarMultimedia(
            @PathVariable String id,
            @RequestPart("file") MultipartFile file,
            @RequestPart("tipoArchivo") String tipoArchivo,
            @RequestPart(value = "descripcion", required = false) String descripcion,
            @RequestPart(value = "etapa", required = false) String etapa
    ) throws java.io.IOException {
        MultimediaResult result = ordenService.agregarMultimedia(
                id, file.getBytes(), tipoArchivo, descripcion, etapa
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toMultimediaResponse(result));
    }

    @PostMapping("/{id}/multimedia/presign")
    public ResponseEntity<MultimediaPresignResponse> prepararMultimedia(
            @PathVariable String id,
            @Valid @RequestBody MultimediaPresignRequest request
    ) {
        MultimediaPresignResult result = ordenService.prepararMultimedia(id, request.tipoArchivo());
        return ResponseEntity.ok(new MultimediaPresignResponse(
                result.presignedUrl(), result.objectKey(), result.publicUrl()
        ));
    }

    @PostMapping("/{id}/multimedia/confirm")
    public ResponseEntity<OrdenMultimediaResponse> confirmarMultimedia(
            @PathVariable String id,
            @Valid @RequestBody MultimediaConfirmRequest request
    ) {
        MultimediaConfirmationResult result = ordenService.confirmarMultimedia(
                id,
                request.objectKey(),
                request.publicUrl(),
                request.tipoArchivo(),
                request.descripcion(),
                request.etapa()
        );
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(toMultimediaResponse(result.multimedia()));
    }

    @PostMapping("/{id}/productos")
    public ResponseEntity<List<OrdenProductoResponse>> agregarProductos(
            @PathVariable String id,
            @Valid @NotEmpty @Size(max = 50) @RequestBody List<@Valid OrdenProductoAddRequest> items
    ) {
        validarIdentificador(id);
        List<OrdenProductoAddCommand> commands = items.stream()
                .map(this::toProductoCommand)
                .toList();
        List<OrdenProductoResult> results = parseUuid(id)
                .map(uuid -> ordenService.agregarProductos(uuid, commands))
                .orElseGet(() -> ordenService.agregarProductos(id, commands));
        List<OrdenProductoResponse> response = results.stream()
                .map(this::toProductoResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/servicios")
    public ResponseEntity<List<OrdenServicioResponse>> agregarServicios(
            @PathVariable String id,
            @Valid @NotEmpty @Size(max = 50) @RequestBody List<@Valid OrdenServicioAddRequest> items
    ) {
        validarIdentificador(id);
        List<OrdenServicioAddCommand> commands = items.stream()
                .map(this::toServicioCommand)
                .toList();
        List<OrdenServicioResult> results = parseUuid(id)
                .map(uuid -> ordenService.agregarServicios(uuid, commands))
                .orElseGet(() -> ordenService.agregarServicios(id, commands));
        List<OrdenServicioResponse> response = results.stream()
                .map(this::toServicioResponse)
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Convierte un OrdenReadResult a OrdenResumenResponse, mapeando solo los campos necesarios para el resumen. 
     * Este método se encarga de transformar la información del dominio al formato que se expondrá a través de la API REST en el endpoint de resumen.
     * @param r
     * @return
     */
    private OrdenResumenResponse toResumenResponse(OrdenReadResult r) {
        String mecanico = r.mecanicoId() != null
            ? r.mecanicoNombre() + " " + r.mecanicoApellido()
            : "No asignado";
        return new OrdenResumenResponse(
            r.numeroOrden(),
            r.tipoNombre(),
            r.fechaIngreso(),
            mecanico,
            r.clienteNombre() + " " + r.clienteApellido(),
            new OrdenResumenResponse.BicicletaResumenResponse(
                r.bicicletaMarca(), r.bicicletaModelo(), r.bicicletaTipo(), r.bicicletaColor()
            ),
            r.diagnosticoInicial(),
            r.estadoNombre(),
            r.prioridad()
        );
    }

    /**
     * Convierte un OrdenDetalleResult a OrdenDetalleResponse, mapeando todos los campos necesarios. Este método se encarga de transformar la información del dominio al formato que se expondrá a través 
     * de la API REST.
     * @param result
     * @return
     */
    private OrdenDetalleResponse toDetalleResponse(OrdenDetalleResult result) {
        OrdenDetalleResponse.MecanicoDetalleResponse mecanico = null;
        if (result.mecanicoId() != null) {
            mecanico = new OrdenDetalleResponse.MecanicoDetalleResponse(
                result.mecanicoId(),
                result.mecanicoNombre(),
                result.mecanicoApellido()
            );
        }

        List<OrdenDetalleResponse.ComentarioResponse> comentarios = result.comentarios() != null
            ? result.comentarios().stream()
            .map(c -> new OrdenDetalleResponse.ComentarioResponse(
                    c.id(), c.usuarioId(), c.usuario(), c.texto(), c.createdAt()
            ))
            .toList()
            : List.of();

        List<OrdenDetalleResponse.MultimediaResponse> multimedia = result.multimedia() != null
            ? result.multimedia().stream()
            .map(m -> new OrdenDetalleResponse.MultimediaResponse(
                m.id(), m.usuarioId(), m.usuario(), m.tipoArchivo(), m.categoria(),
                m.url(), m.etapa(), m.descripcion(), m.createdAt()
            ))
            .toList()
            : List.of();

        List<OrdenDetalleResponse.ProductoResponse> productos = result.productos() != null
            ? result.productos().stream()
            .map(p -> new OrdenDetalleResponse.ProductoResponse(
                p.id(),
                p.productoId(),
                p.nombre(),
                p.sku(),
                p.cantidad(),
                p.precioVenta(),
                p.precioAplicado(),
                p.notas(),
                p.proporcionadoPorCliente(),
                p.usuarioId(),
                p.usuario(),
                p.createdAt()
            ))
            .toList()
            : List.of();

        List<OrdenDetalleResponse.ServicioResponse> servicios = result.servicios() != null
            ? result.servicios().stream()
            .map(s -> new OrdenDetalleResponse.ServicioResponse(
                s.id(),
                s.servicioId(),
                s.nombre(),
                s.precioBase(),
                s.precioAplicado(),
                s.descuentoAplicado(),
                s.notas(),
                s.usuarioId(),
                s.usuario(),
                s.createdAt()
            ))
            .toList()
            : List.of();

        return new OrdenDetalleResponse(
            result.id(),
            result.numeroOrden(),
            result.tallerId(),
            result.sucursalId(),
            new OrdenDetalleResponse.CatalogoResponse(
                result.estadoId(), result.estadoCodigo(), result.estadoNombre()
            ),
            new OrdenDetalleResponse.CatalogoResponse(
                result.tipoId(), result.tipoCodigo(), result.tipoNombre()
            ),
            result.fechaIngreso(),
            result.fechaPrometida(),
            result.fechaEntrega(),
            result.diagnosticoInicial(),
            result.diagnosticoFinal(),
            result.observacionesCliente(),
            new OrdenDetalleResponse.BicicletaDetalleResponse(
                result.bicicletaId(),
                result.bicicletaMarca(),
                result.bicicletaModelo(),
                result.bicicletaTipo(),
                result.bicicletaAro(),
                result.bicicletaColor(),
                result.bicicletaNumeroSerie(),
                result.bicicletaAnio(),
                result.bicicletaFotoUrl(),
                result.bicicletaNotas()
            ),
            new OrdenDetalleResponse.ClienteDetalleResponse(
                result.clienteId(),
                result.clienteNombre(),
                result.clienteApellido(),
                result.clienteTelefono(),
                result.clienteEmail(),
                result.clienteRut(),
                result.clienteDireccion()
            ),
            mecanico,
            result.prioridad(),
            result.servicioResumen(),
            result.montoTotal(),
            comentarios,
            multimedia,
            productos,
            servicios,
            result.historialEstados().stream()
                    .map(history -> new OrdenDetalleResponse.EstadoHistoryResponse(
                            history.id(),
                            history.estadoAnteriorId() == null ? null : new OrdenDetalleResponse.CatalogoResponse(
                                    history.estadoAnteriorId(),
                                    history.estadoAnteriorCodigo(),
                                    history.estadoAnteriorNombre()
                            ),
                            new OrdenDetalleResponse.CatalogoResponse(
                                    history.estadoNuevoId(),
                                    history.estadoNuevoCodigo(),
                                    history.estadoNuevoNombre()
                            ),
                            history.observacion(),
                            history.usuarioId(),
                            history.usuario(),
                            history.createdAt()
                    ))
                    .toList()
        );
    }

    /**
     * Convierte un NuevaOrdenRequest a OrdenCreateCommand, mapeando todos los campos necesarios. Este método se encarga de transformar la información recibida en el request al formato que el servicio de 
     * aplicación espera para crear una nueva orden.
     * @param r
     * @return
     */
    private OrdenCreateCommand toCommand(OrdenCreateRequest r) {
        ClienteCreateCommand clienteNuevo = null;
        if (r.getClienteNuevo() != null) {
            OrdenCreateRequest.ClienteCreateRequest cn = r.getClienteNuevo();
            clienteNuevo = new ClienteCreateCommand(cn.getNombre(), cn.getApellido(),
                    cn.getRut(), cn.getTelefono(), cn.getEmail(), cn.getDireccion());
        }

        BicicletaCreateCommand bicicletaNueva = null;
        if (r.getBicicletaNueva() != null) {
            OrdenCreateRequest.BicicletaCreateRequest bn = r.getBicicletaNueva();
            bicicletaNueva = new BicicletaCreateCommand(bn.getMarca(), bn.getModelo(),
                    bn.getTipo(), bn.getAro(), bn.getColor(), bn.getNumeroSerie(), bn.getAnio(), bn.getNotas());
        }

        List<java.util.UUID> servicios = r.getServicios() != null
                ? r.getServicios().stream().map(OrdenCreateRequest.ServicioItem::getServicioId).collect(Collectors.toList())
                : null;

        List<OrdenCreateCommand.ProductoItem> productos = r.getProductos() != null
                ? r.getProductos().stream()
                        .map(p -> new OrdenCreateCommand.ProductoItem(p.getProductoId(), p.getCantidad()))
                        .collect(Collectors.toList())
                : null;

        return OrdenCreateCommand.builder()
                .clienteId(r.getClienteId())
                .clienteNuevo(clienteNuevo)
                .bicicletaId(r.getBicicletaId())
                .bicicletaNueva(bicicletaNueva)
                .sucursalId(r.getSucursalId())
                .tipoTrabajo(r.getTipoTrabajo())
                .prioridad(r.getPrioridad())
                .mecanicoId(r.getMecanicoId())
                .fechaPrometida(r.getFechaPrometida())
                .diagnosticoInicial(r.getDiagnosticoInicial())
                .observacionesCliente(r.getObservacionesCliente())
                .servicios(servicios)
                .productos(productos)
                .build();
    }

    private OrdenProductoAddCommand toProductoCommand(OrdenProductoAddRequest request) {
        return new OrdenProductoAddCommand(
                request.getProductoId(),
                request.getCantidad(),
                request.getProporcionadoPorCliente(),
                request.getNotas()
        );
    }

    private OrdenProductoUpdateCommand toProductoUpdateCommand(OrdenProductoUpdateRequest request) {
        return new OrdenProductoUpdateCommand(
                request.getId(),
                request.getCantidad(),
                request.getProporcionadoPorCliente(),
                request.getNotas()
        );
    }

    private void mapProductoCambios(List<OrdenProductoCambioRequest> cambios,
                                    List<OrdenProductoAddCommand> productosAgregar,
                                    List<OrdenProductoUpdateCommand> productosActualizar,
                                    List<UUID> productosEliminar) {
        if (cambios == null) {
            return;
        }

        for (OrdenProductoCambioRequest cambio : cambios) {
            String accion = cambio.getAccion() != null ? cambio.getAccion().trim().toUpperCase() : "";
            switch (accion) {
                case "AGREGAR" -> productosAgregar.add(new OrdenProductoAddCommand(
                        cambio.getProductoId(),
                        cambio.getCantidad(),
                        cambio.getProporcionadoPorCliente(),
                        cambio.getNotas()
                ));
                case "ACTUALIZAR" -> productosActualizar.add(new OrdenProductoUpdateCommand(
                        cambio.getLineaId(),
                        cambio.getCantidad(),
                        cambio.getProporcionadoPorCliente(),
                        cambio.getNotas()
                ));
                case "ELIMINAR" -> productosEliminar.add(cambio.getLineaId());
                default -> throw new com.veloservice.shared.application.exception.BadRequestException(
                        "accion de producto invalida: " + cambio.getAccion()
                );
            }
        }
    }

    private void mapServicioCambios(List<OrdenServicioCambioRequest> cambios,
                                    List<OrdenServicioAddCommand> serviciosAgregar,
                                    List<OrdenServicioUpdateCommand> serviciosActualizar,
                                    List<UUID> serviciosEliminar) {
        if (cambios == null) {
            return;
        }

        for (OrdenServicioCambioRequest cambio : cambios) {
            String accion = cambio.getAccion() != null ? cambio.getAccion().trim().toUpperCase() : "";
            switch (accion) {
                case "AGREGAR" -> serviciosAgregar.add(new OrdenServicioAddCommand(
                        cambio.getServicioId(),
                        cambio.getNotas()
                ));
                case "ACTUALIZAR" -> serviciosActualizar.add(new OrdenServicioUpdateCommand(
                        cambio.getLineaId(),
                        cambio.getPrecioAplicado(),
                        cambio.getDescuentoAplicado(),
                        cambio.getNotas()
                ));
                case "ELIMINAR" -> serviciosEliminar.add(cambio.getLineaId());
                default -> throw new com.veloservice.shared.application.exception.BadRequestException(
                        "accion de servicio invalida: " + cambio.getAccion()
                );
            }
        }
    }

    private OrdenServicioAddCommand toServicioCommand(OrdenServicioAddRequest request) {
        return new OrdenServicioAddCommand(request.getServicioId(), request.getNotas());
    }

    private OrdenProductoResponse toProductoResponse(OrdenProductoResult result) {
        return new OrdenProductoResponse(
                result.id(),
                result.productoId(),
                result.nombre(),
                result.sku(),
                result.cantidad(),
                result.precioVenta(),
                result.precioAplicado(),
                result.notas(),
                result.proporcionadoPorCliente(),
                result.usuarioId(),
                result.usuario(),
                result.createdAt()
        );
    }

    private OrdenServicioResponse toServicioResponse(OrdenServicioResult result) {
        return new OrdenServicioResponse(
                result.id(),
                result.servicioId(),
                result.nombre(),
                result.precioBase(),
                result.precioAplicado(),
                result.descuentoAplicado(),
                result.notas(),
                result.usuarioId(),
                result.usuario(),
                result.createdAt()
        );
    }

    /**
     * Convierte un OrdenReadResult a OrdenReadResponse, mapeando todos los campos necesarios. Este método se encarga de transformar la información del dominio al formato que se expondrá a través de la API REST.
     * @param result
     * @return
     */
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
                        result.clienteRut(),
                        result.clienteDireccion()
                ),
                mecanico,
                result.prioridad(),
                result.servicioResumen(),
                result.montoTotal()
        );
    }

    private OrdenComentarioResponse toComentarioResponse(ComentarioResult result) {
        return new OrdenComentarioResponse(
                result.id(), result.usuarioId(), result.usuario(), result.texto(), result.createdAt()
        );
    }

    private OrdenMultimediaResponse toMultimediaResponse(MultimediaResult result) {
        return new OrdenMultimediaResponse(
                result.id(), result.usuarioId(), result.usuario(), result.tipoArchivo(),
                result.categoria(), result.url(), result.etapa(), result.descripcion(), result.createdAt()
        );
    }

    private OrdenCatalogoResponse toCatalogoResponse(OrdenCatalogoResult result) {
        return new OrdenCatalogoResponse(
                result.codigo(),
                result.nombre(),
                result.orden(),
                result.activo()
        );
    }

    private boolean isPrioridadUrgente(String prioridad) {
        return prioridad != null && (
                "alta".equalsIgnoreCase(prioridad) || "urgente".equalsIgnoreCase(prioridad)
        );
    }

    private void validarIdentificador(String id) {
        if (parseUuid(id).isEmpty() && (id == null || !id.matches("(?i)(OT|AP)-[A-Z0-9-]{1,40}"))) {
            throw new com.veloservice.shared.application.exception.BadRequestException(
                    "id tiene un formato invalido"
            );
        }
    }

    private java.util.Optional<UUID> parseUuid(String id) {
        try {
            return java.util.Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException | NullPointerException ex) {
            return java.util.Optional.empty();
        }
    }
}
