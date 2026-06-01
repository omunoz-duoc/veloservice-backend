package com.veloservice.ordenes.interfaces.rest;

import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenCreadaResult;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenDetalleResult;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.application.usecase.OrdenService;
import com.veloservice.ordenes.interfaces.rest.dto.NuevaOrdenRequest;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenCreadaResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenDetalleResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenReadListResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenReadResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenResumenListResponse;
import com.veloservice.ordenes.interfaces.rest.dto.OrdenResumenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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
    @PostMapping
    public ResponseEntity<OrdenCreadaResponse> crear(@Valid @RequestBody NuevaOrdenRequest request) {
        OrdenCreateCommand command = toCommand(request);
        OrdenCreadaResult result = ordenService.crear(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new OrdenCreadaResponse(result.id(), result.numeroOrden()));
    }

    /**
     * TODO: paginar y/o permitir filtros.
     * Lista todas las órdenes de trabajo con detalles completos. Este endpoint puede ser costoso en términos de rendimiento si hay muchas órdenes, por lo que se recomienda usarlo con paginación o filtros en un entorno real.
     *  Para obtener listados más ligeros, se pueden usar otros endpoints como /ordenes/resumen.
     * @return
     */
    @GetMapping
    public ResponseEntity<OrdenReadListResponse> listar() {
        List<OrdenReadResponse> ordenes = ordenService.listar().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(new OrdenReadListResponse(ordenes.size(), ordenes));
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
    @GetMapping("/{id}")
    public ResponseEntity<OrdenDetalleResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(toDetalleResponse(ordenService.obtenerDetalle(id)));
    }

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

        List<OrdenDetalleResponse.ComentarioResponse> comentarios = result.comentarios().stream()
            .map(c -> new OrdenDetalleResponse.ComentarioResponse(c.usuario(), c.texto(), c.createdAt()))
            .toList();

        List<OrdenDetalleResponse.MultimediaResponse> multimedia = result.multimedia().stream()
            .map(m -> new OrdenDetalleResponse.MultimediaResponse(
                m.usuario(), m.tipoArchivo(), m.url(), m.etapa(), m.descripcion()
            ))
            .toList();

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
                result.bicicletaColor(),
                result.bicicletaNumeroSerie()
            ),
            new OrdenDetalleResponse.ClienteDetalleResponse(
                result.clienteId(),
                result.clienteNombre(),
                result.clienteApellido(),
                result.clienteTelefono(),
                result.clienteEmail(),
                result.clienteRut()
            ),
            mecanico,
            result.prioridad(),
            comentarios,
            multimedia
        );
    }

    /**
     * Convierte un NuevaOrdenRequest a OrdenCreateCommand, mapeando todos los campos necesarios. Este método se encarga de transformar la información recibida en el request al formato que el servicio de 
     * aplicación espera para crear una nueva orden.
     * @param r
     * @return
     */
    private OrdenCreateCommand toCommand(NuevaOrdenRequest r) {
        ClienteCreateCommand clienteNuevo = null;
        if (r.getClienteNuevo() != null) {
            NuevaOrdenRequest.ClienteNuevoRequest cn = r.getClienteNuevo();
            clienteNuevo = new ClienteCreateCommand(cn.getNombre(), cn.getApellido(),
                    cn.getRut(), cn.getTelefono(), cn.getEmail(), cn.getDireccion());
        }

        BicicletaCreateCommand bicicletaNueva = null;
        if (r.getBicicletaNueva() != null) {
            NuevaOrdenRequest.BicicletaNuevaRequest bn = r.getBicicletaNueva();
            bicicletaNueva = new BicicletaCreateCommand(bn.getMarca(), bn.getModelo(),
                    bn.getTipo(), bn.getAro(), bn.getColor(), bn.getNumeroSerie(), bn.getAnio(), bn.getNotas());
        }

        List<java.util.UUID> servicios = r.getServicios() != null
                ? r.getServicios().stream().map(NuevaOrdenRequest.ServicioItem::getServicioId).collect(Collectors.toList())
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
                        result.clienteRut()
                ),
                mecanico,
                result.prioridad()
        );
    }
}
