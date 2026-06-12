package com.veloservice.ordenes.application.usecase;

import com.veloservice.administracion.domain.model.UsuarioSucursal;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.administracion.infraestructure.persistence.repository.UsuarioSucursalRepository;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.clientes.application.dto.BicicletaCreateCommand;
import com.veloservice.clientes.application.dto.ClienteCreateCommand;
import com.veloservice.clientes.application.usecase.BicicletaService;
import com.veloservice.clientes.application.usecase.ClienteService;
import com.veloservice.clientes.domain.model.Bicicleta;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.ClienteRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.config.tenant.UsuarioContext;
import com.veloservice.ordenes.domain.AccionHistorialEnum;
import com.veloservice.ordenes.application.dto.OrdenHistorialResult;
import com.veloservice.config.storage.R2Properties;
import com.veloservice.auth.infraestructure.persistence.repository.UsuarioRepository;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.ordenes.application.dto.ComentarioResult;
import com.veloservice.ordenes.application.dto.MultimediaResult;
import com.veloservice.ordenes.application.dto.MultimediaConfirmationResult;
import com.veloservice.ordenes.application.dto.MultimediaPresignResult;
import com.veloservice.ordenes.application.dto.OrdenCatalogoResult;
import com.veloservice.ordenes.application.dto.OrdenCreateResult;
import com.veloservice.ordenes.application.dto.OrdenCreateCommand;
import com.veloservice.ordenes.application.dto.OrdenDetalleBaseResult;
import com.veloservice.ordenes.application.dto.OrdenDetalleResult;
import com.veloservice.ordenes.application.dto.OrdenEstadoChangeCommand;
import com.veloservice.ordenes.application.dto.OrdenEstadoResult;
import com.veloservice.ordenes.application.dto.OrdenMetricasResult;
import com.veloservice.ordenes.application.dto.OrdenProductoAddCommand;
import com.veloservice.ordenes.application.dto.OrdenProductoResult;
import com.veloservice.ordenes.application.dto.OrdenProductoUpdateCommand;
import com.veloservice.ordenes.application.dto.OrdenReadResult;
import com.veloservice.ordenes.application.dto.OrdenServicioAddCommand;
import com.veloservice.ordenes.application.dto.OrdenServicioResult;
import com.veloservice.ordenes.application.dto.OrdenServicioUpdateCommand;
import com.veloservice.ordenes.application.dto.OrdenUpdateCommand;
import com.veloservice.ordenes.application.port.MediaStoragePort;
import com.veloservice.ordenes.application.port.R2StoragePort;
import com.veloservice.ordenes.domain.EtapaMultimediaEnum;
import com.veloservice.ordenes.domain.TipoArchivoEnum;
import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.ordenes.domain.PrioridadOrdenEnum;
import com.veloservice.ordenes.domain.model.EstadoOrden;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenEstado;
import com.veloservice.ordenes.domain.model.OrdenProducto;
import com.veloservice.ordenes.domain.model.OrdenServicio;
import com.veloservice.ordenes.domain.model.OrdenComentario;
import com.veloservice.ordenes.domain.model.Multimedia;
import com.veloservice.ordenes.domain.model.TipoOrden;
import com.veloservice.ordenes.infraestructure.persistence.repository.ComentarioRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.EstadoOrdenCatalogRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.MultimediaRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenEstadoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.TipoOrdenRepository;
import com.veloservice.servicios.domain.model.Servicio;
import com.veloservice.servicios.domain.model.SucursalServicio;
import com.veloservice.servicios.infraestructure.persistence.repository.ServicioRepository;
import com.veloservice.servicios.infraestructure.persistence.repository.SucursalServicioRepository;
import com.veloservice.shared.application.exception.BadRequestException;
import com.veloservice.shared.application.exception.ConflictException;
import com.veloservice.shared.application.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.time.ZoneOffset;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.io.IOException;
import java.util.Arrays;

@Service
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ComentarioRepository comentarioRepository;
    private final MultimediaRepository multimediaRepository;
    private final EstadoOrdenCatalogRepository estadoOrdenRepository;
    private final OrdenEstadoRepository ordenEstadoRepository;
    private final TipoOrdenRepository tipoOrdenRepository;
    private final ServicioRepository servicioRepository;
    private final SucursalServicioRepository sucursalServicioRepository;
    private final ProductoRepository productoRepository;
    private final OrdenServicioRepository ordenServicioRepository;
    private final OrdenProductoRepository ordenProductoRepository;
    private final SecuenciaService secuenciaService;
    private final ClienteService clienteService;
    private final BicicletaService bicicletaService;
    private final BicicletaRepository bicicletaRepository;
    private final ClienteRepository clienteRepository;
    private final SucursalRepository sucursalRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioSucursalRepository usuarioSucursalRepository;
    private final MediaStoragePort mediaStorage;
    private final R2StoragePort r2Storage;
    private final R2Properties r2Properties;
    private final OrdenHistorialService ordenHistorialService;

    @org.springframework.beans.factory.annotation.Autowired
    public OrdenService(
            OrdenRepository ordenRepository,
            ComentarioRepository comentarioRepository,
            MultimediaRepository multimediaRepository,
            EstadoOrdenCatalogRepository estadoOrdenRepository,
            OrdenEstadoRepository ordenEstadoRepository,
            TipoOrdenRepository tipoOrdenRepository,
            ServicioRepository servicioRepository,
            SucursalServicioRepository sucursalServicioRepository,
            ProductoRepository productoRepository,
            OrdenServicioRepository ordenServicioRepository,
            OrdenProductoRepository ordenProductoRepository,
            SecuenciaService secuenciaService,
            ClienteService clienteService,
            BicicletaService bicicletaService,
            BicicletaRepository bicicletaRepository,
            ClienteRepository clienteRepository,
            SucursalRepository sucursalRepository,
            UsuarioRepository usuarioRepository,
            UsuarioSucursalRepository usuarioSucursalRepository,
            MediaStoragePort mediaStorage,
            R2StoragePort r2Storage,
            R2Properties r2Properties,
            OrdenHistorialService ordenHistorialService
    ) {
        this.ordenRepository = ordenRepository;
        this.comentarioRepository = comentarioRepository;
        this.multimediaRepository = multimediaRepository;
        this.estadoOrdenRepository = estadoOrdenRepository;
        this.ordenEstadoRepository = ordenEstadoRepository;
        this.tipoOrdenRepository = tipoOrdenRepository;
        this.servicioRepository = servicioRepository;
        this.sucursalServicioRepository = sucursalServicioRepository;
        this.productoRepository = productoRepository;
        this.ordenServicioRepository = ordenServicioRepository;
        this.ordenProductoRepository = ordenProductoRepository;
        this.secuenciaService = secuenciaService;
        this.clienteService = clienteService;
        this.bicicletaService = bicicletaService;
        this.bicicletaRepository = bicicletaRepository;
        this.clienteRepository = clienteRepository;
        this.sucursalRepository = sucursalRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioSucursalRepository = usuarioSucursalRepository;
        this.mediaStorage = mediaStorage;
        this.r2Storage = r2Storage;
        this.r2Properties = r2Properties;
        this.ordenHistorialService = ordenHistorialService;
    }

    public OrdenService(
            OrdenRepository ordenRepository,
            ComentarioRepository comentarioRepository,
            MultimediaRepository multimediaRepository,
            EstadoOrdenCatalogRepository estadoOrdenRepository,
            OrdenEstadoRepository ordenEstadoRepository,
            TipoOrdenRepository tipoOrdenRepository,
            ServicioRepository servicioRepository,
            SucursalServicioRepository sucursalServicioRepository,
            ProductoRepository productoRepository,
            OrdenServicioRepository ordenServicioRepository,
            OrdenProductoRepository ordenProductoRepository,
            SecuenciaService secuenciaService,
            ClienteService clienteService,
            BicicletaService bicicletaService,
            BicicletaRepository bicicletaRepository,
            ClienteRepository clienteRepository,
            SucursalRepository sucursalRepository,
            UsuarioRepository usuarioRepository,
            UsuarioSucursalRepository usuarioSucursalRepository,
            OrdenHistorialService ordenHistorialService
    ) {
        this(ordenRepository, comentarioRepository, multimediaRepository, estadoOrdenRepository,
                ordenEstadoRepository, tipoOrdenRepository, servicioRepository,
                sucursalServicioRepository, productoRepository, ordenServicioRepository,
                ordenProductoRepository, secuenciaService, clienteService, bicicletaService,
                bicicletaRepository, clienteRepository, sucursalRepository, usuarioRepository,
                usuarioSucursalRepository, null, null, null, ordenHistorialService);
    }

    /**
     * Lista todas las órdenes asociadas al taller o sucursal actual. Si ambos contextos están presentes, se prioriza el contexto de 
     * sucursal.
     * @return
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenReadResult> listar() {
        return listar(null);
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenReadResult> listar(UUID requestedSucursalId) {
        UUID resolvedSucursalId = resolverSucursalConsulta(requestedSucursalId);
        List<OrdenReadResult> results;
        if (resolvedSucursalId != null) {
            results = ordenRepository.findReadBySucursalId(resolvedSucursalId);
        } else {
            UUID tallerId = TallerContext.getCurrentTaller();
            if (tallerId == null) {
                throw new IllegalStateException("Contexto de taller o sucursal requerido");
            }
            results = ordenRepository.findReadByTallerId(tallerId);
        }
        return results.stream().map(this::enriquecerResumen).toList();
    }

    /**
     * Lista las órdenes asignadas a un mecánico. La sucursal se deriva de la sucursal principal del propio mecánico y se valida
     * contra el alcance autorizado del solicitante (contexto de sucursal o taller), garantizando aislamiento multi-tenant.
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenReadResult> listarPorMecanico(UUID mecanicoId) {
        UUID mecanicoSucursalId = usuarioSucursalRepository.findByUsuarioIdAndEsPrincipalTrue(mecanicoId)
                .map(UsuarioSucursal::getSucursalId)
                .orElseThrow(() -> new IllegalArgumentException("El mecánico no tiene una sucursal principal asignada"));
        UUID resolvedSucursalId = resolverSucursalConsulta(mecanicoSucursalId);
        if (resolvedSucursalId == null) {
            throw new IllegalStateException("Contexto de taller o sucursal requerido");
        }
        return ordenRepository.findReadBySucursalIdAndMecanicoId(resolvedSucursalId, mecanicoId).stream()
                .map(this::enriquecerResumen)
                .toList();
    }

    private UUID resolverSucursalConsulta(UUID requestedSucursalId) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            if (requestedSucursalId != null && !sucursalId.equals(requestedSucursalId)) {
                throw new AccessDeniedException("Sucursal fuera del alcance autorizado");
            }
            return sucursalId;
        }
        UUID tallerId = TallerContext.getCurrentTaller();
        if (requestedSucursalId != null) {
            if (tallerId == null || !sucursalRepository.existsByIdAndTallerId(requestedSucursalId, tallerId)) {
                throw new AccessDeniedException("Sucursal fuera del alcance autorizado");
            }
            return requestedSucursalId;
        }
        return null;
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenMetricasResult metricas(UUID requestedSucursalId) {
        UUID sucursalId = resolverSucursalConsulta(requestedSucursalId);
        List<OrdenReadResult> orders = sucursalId != null
                ? ordenRepository.findReadBySucursalId(sucursalId)
                : ordenRepository.findReadByTallerId(TallerContext.getCurrentTaller());
        long recibidas = countEstado(orders, "recibida");
        long enProceso = orders.stream()
                .filter(order -> Set.of("en_diagnostico", "esperando_repuestos", "en_reparacion", "control_calidad")
                        .contains(order.estadoCodigo()))
                .count();
        return new OrdenMetricasResult(
                recibidas,
                enProceso,
                countEstado(orders, "lista_para_entrega"),
                countEstado(orders, "entregada")
        );
    }

    @Transactional(readOnly = true)
    public List<OrdenCatalogoResult> listarEstadosCatalogo() {
        return estadoOrdenRepository.findAllByOrderByOrdenAsc().stream()
                .map(estado -> new OrdenCatalogoResult(
                        estado.getCodigo(),
                        estado.getNombre(),
                        estado.getOrden(),
                        null
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenCatalogoResult> listarTiposCatalogo() {
        return tipoOrdenRepository.findAllByOrderByCodigoAsc().stream()
                .map(tipo -> new OrdenCatalogoResult(
                        tipo.getCodigo(),
                        tipo.getNombre(),
                        null,
                        tipo.getActivo()
                ))
                .toList();
    }

    public List<OrdenCatalogoResult> listarPrioridadesCatalogo() {
        return List.of(PrioridadOrdenEnum.baja, PrioridadOrdenEnum.media,
                        PrioridadOrdenEnum.alta, PrioridadOrdenEnum.urgente).stream()
                .map(prioridad -> new OrdenCatalogoResult(prioridad.name(), nombrePrioridad(prioridad), null, true))
                .toList();
    }

    /**
     * Obtiene una orden por su ID o número de orden, buscando primero en el contexto de sucursal (si está presente) y luego en el 
     * contexto de taller.
     * @param id
     * @return
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenReadResult obtener(String id) {
        UUID tallerId = TallerContext.getCurrentTaller();
        UUID sucursalId = SucursalContext.getCurrentSucursal();

        if (sucursalId != null) {
            return buscarEnSucursal(id, sucursalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        }

        if (tallerId != null) {
            return buscarEnTaller(id, tallerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        }

        throw new IllegalStateException("Contexto de taller o sucursal requerido");
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public OrdenDetalleResult obtenerDetalle(String id) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID tallerId = TallerContext.getCurrentTaller();

        OrdenDetalleBaseResult base;
        if (sucursalId != null) {
            base = buscarDetalleBaseEnSucursal(id, sucursalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        } else if (tallerId != null) {
            base = buscarDetalleBaseEnTaller(id, tallerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        } else {
            throw new IllegalStateException("Contexto de taller o sucursal requerido");
        }

        List<ComentarioResult> comentarios = comentarioRepository.findResultByOrdenId(base.id());
        List<MultimediaResult> multimedia = multimediaRepository.findResultByOrdenId(base.id());
        List<OrdenProductoResult> productos = ordenProductoRepository.findResultByOrdenId(base.id());
        List<OrdenServicioResult> servicios = ordenServicioRepository.findResultByOrdenId(base.id());
        List<OrdenEstadoResult> historialEstados = ordenEstadoRepository.findResultByOrdenId(base.id());
        String servicioResumen = servicios.isEmpty() ? null : servicios.getFirst().nombre();
        BigDecimal montoTotal = calcularMontoTotal(base.id());
        return new OrdenDetalleResult(
            base.id(),
            base.numeroOrden(),
            base.tallerId(),
            base.sucursalId(),
            base.estadoId(), base.estadoCodigo(), base.estadoNombre(),
            base.tipoId(), base.tipoCodigo(), base.tipoNombre(),
            base.fechaIngreso(),
            base.fechaPrometida(),
            base.fechaEntrega(),
            base.diagnosticoInicial(),
            base.diagnosticoFinal(),
            base.observacionesCliente(),
            base.bicicletaId(), base.bicicletaMarca(), base.bicicletaModelo(), base.bicicletaTipo(), base.bicicletaColor(), base.bicicletaNumeroSerie(), base.bicicletaAro(), base.bicicletaAnio(), base.bicicletaFotoUrl(), base.bicicletaNotas(),
            base.clienteId(), base.clienteNombre(), base.clienteApellido(),
            base.clienteTelefono(), base.clienteEmail(), base.clienteRut(),
            base.clienteDireccion(),
            base.mecanicoId(), base.mecanicoNombre(), base.mecanicoApellido(),
            base.prioridad(),
            servicioResumen,
            montoTotal,
            comentarios,
            multimedia,
            productos,
            servicios,
            historialEstados
        );
    }

    @TenantOperation
    @Transactional
    public ComentarioResult agregarComentario(String id, String texto) {
        Orden orden = buscarOrdenParaMutacion(id);
        String trimmed = texto == null ? "" : texto.trim();
        if (trimmed.isEmpty() || trimmed.length() > 4000) {
            throw new BadRequestException("texto debe contener entre 1 y 4000 caracteres");
        }
        OrdenComentario saved = comentarioRepository.save(OrdenComentario.builder()
                .ordenId(orden.getId())
                .usuarioId(requerirUsuarioContext())
                .texto(trimmed)
                .createdAt(OffsetDateTime.now())
                .build());
        return comentarioRepository.findResultById(saved.getId())
                .orElseThrow(() -> new IllegalStateException("No fue posible leer el comentario creado"));
    }

    @TenantOperation
    @Transactional
    public MultimediaResult agregarMultimedia(
            String id,
            byte[] content,
            String declaredContentType,
            String descripcion,
            String etapa
    ) {
        Orden orden = buscarOrdenParaMutacion(id);
        MediaTypeInfo mediaType = validarMedia(content, declaredContentType);
        EtapaMultimediaEnum etapaEnum = parseEtapa(etapa);
        String descripcionNormalizada = normalizarOpcional(descripcion, 500, "descripcion");
        MediaStoragePort.StoredMedia stored;
        try {
            stored = mediaStorage.store(orden.getTallerId(), orden.getId(), mediaType.mimeType(), content);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Almacenamiento no disponible", ex);
        }
        try {
            Multimedia saved = multimediaRepository.save(Multimedia.builder()
                    .ordenId(orden.getId())
                    .usuarioId(requerirUsuarioContext())
                    .url(stored.url())
                    .tipoArchivo(mediaType.mimeType())
                    .etapa(etapaEnum)
                    .descripcion(descripcionNormalizada)
                    .createdAt(OffsetDateTime.now())
                    .build());
            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.MULTIMEDIA_AGREGADA,
                    "multimedia",
                    saved.getId(),
                    java.util.Map.of(
                            "tipoArchivo", saved.getTipoArchivo() == null ? "" : saved.getTipoArchivo(),
                            "etapa", saved.getEtapa() == null ? "" : saved.getEtapa().name()
                    ));
            return multimediaRepository.findResultById(saved.getId())
                    .orElseThrow(() -> new IllegalStateException("No fue posible leer el archivo creado"));
        } catch (RuntimeException ex) {
            mediaStorage.delete(stored.key());
            throw ex;
        }
    }

    @TenantOperation
    @Transactional(readOnly = true)
    public MultimediaPresignResult prepararMultimedia(String id, String tipoArchivo) {
        Orden orden = buscarOrdenParaMutacion(id);
        String mimeType = validarMimePermitido(tipoArchivo);
        String objectKey = "ordenes/" + orden.getId() + "/" + UUID.randomUUID() + extensionParaMime(mimeType);
        Duration expiry = r2Properties.presignExpiry() != null
                ? r2Properties.presignExpiry()
                : Duration.ofMinutes(15);
        String presignedUrl = r2Storage.presignPut(objectKey, mimeType, expiry).url();
        return new MultimediaPresignResult(presignedUrl, objectKey, publicUrlPara(objectKey));
    }

    @TenantOperation
    @Transactional
    public MultimediaConfirmationResult confirmarMultimedia(
            String id,
            String objectKey,
            String publicUrl,
            String tipoArchivo,
            String descripcion,
            String etapa
    ) {
        Orden orden = buscarOrdenParaMutacion(id);
        String mimeType = validarMimePermitido(tipoArchivo);
        validarObjectKey(orden.getId(), objectKey, mimeType);
        String expectedPublicUrl = publicUrlPara(objectKey);
        if (!expectedPublicUrl.equals(publicUrl)) {
            throw new BadRequestException("publicUrl no corresponde al objectKey");
        }

        R2StoragePort.ObjectMetadata metadata = r2Storage.head(objectKey)
                .orElseThrow(() -> new BadRequestException("El archivo no existe en R2"));
        if (!mimeType.equals(metadata.contentType())) {
            throw new BadRequestException("tipoArchivo no coincide con el archivo subido");
        }
        if (metadata.contentLength() <= 0) {
            throw new BadRequestException("El archivo subido esta vacio");
        }
        if (metadata.contentLength() > maxSizeParaMime(mimeType)) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Archivo demasiado grande");
        }

        Optional<Multimedia> existing = multimediaRepository.findByObjectKey(objectKey);
        if (existing.isPresent()) {
            Multimedia multimedia = existing.get();
            if (!orden.getId().equals(multimedia.getOrdenId())) {
                throw new ConflictException("El archivo ya fue confirmado para otra orden");
            }
            MultimediaResult result = multimediaRepository.findResultById(multimedia.getId())
                    .orElseThrow(() -> new IllegalStateException("No fue posible leer el archivo confirmado"));
            return new MultimediaConfirmationResult(result, false);
        }

        Multimedia saved = multimediaRepository.save(Multimedia.builder()
                .ordenId(orden.getId())
                .usuarioId(requerirUsuarioContext())
                .url(expectedPublicUrl)
                .objectKey(objectKey)
                .tipoArchivo(mimeType)
                .etapa(parseEtapa(etapa))
                .descripcion(normalizarOpcional(descripcion, 500, "descripcion"))
                .createdAt(OffsetDateTime.now())
                .build());
        ordenHistorialService.registrar(
                orden.getId(),
                AccionHistorialEnum.MULTIMEDIA_AGREGADA,
                "multimedia",
                saved.getId(),
                java.util.Map.of(
                        "tipoArchivo", saved.getTipoArchivo() == null ? "" : saved.getTipoArchivo(),
                        "etapa", saved.getEtapa() == null ? "" : saved.getEtapa().name()
                ));
        MultimediaResult result = multimediaRepository.findResultById(saved.getId())
                .orElseThrow(() -> new IllegalStateException("No fue posible leer el archivo creado"));
        return new MultimediaConfirmationResult(result, true);
    }

    @TenantOperation
    @Transactional
    public OrdenCreateResult crear(OrdenCreateCommand command) {
        UUID tallerId = TallerContext.getCurrentTaller();
        UUID contextSucursalId = SucursalContext.getCurrentSucursal();

        if (tallerId == null) {
            throw new IllegalStateException("Se requiere contexto de taller para crear una orden");
        }

        UUID sucursalId = resolverSucursal(tallerId, contextSucursalId, command.getSucursalId());
        boolean sucursalTemporal = contextSucursalId == null;
        if (sucursalTemporal) {
            SucursalContext.setCurrentSucursal(sucursalId);
        }

        try {
            return crearEnSucursal(command, tallerId, sucursalId);
        } finally {
            if (sucursalTemporal) {
                SucursalContext.clear();
            }
        }
    }

    @TenantOperation
    @Transactional
    public void cambiarEstado(String id, OrdenEstadoChangeCommand command) {
        UUID usuarioId = requerirUsuarioContext();
        Orden orden = buscarOrdenParaMutacion(id);
        OffsetDateTime now = OffsetDateTime.now();
        aplicarCambioEstado(orden, command.getCodigo(), command.getObservacion(), now, usuarioId);
        orden.setUpdatedAt(now);
        ordenRepository.save(orden);
    }

    @TenantOperation
    @Transactional
    public OrdenDetalleResult actualizar(String id, OrdenUpdateCommand command) {
        Orden orden = buscarOrdenParaMutacion(id);
        OffsetDateTime now = OffsetDateTime.now();
        boolean changed = false;
        java.util.Map<String, Object> camposEditados = new java.util.LinkedHashMap<>();

        if (hasText(command.getEstadoCodigo())) {
            aplicarCambioEstado(orden, command.getEstadoCodigo(), command.getEstadoObservacion(), now, requerirUsuarioContext());
            changed = true;
        } else if (command.getEstadoCodigo() != null) {
            throw new BadRequestException("estadoCodigo no puede estar vacio");
        }

        if (hasText(command.getTipoCodigo())) {
            TipoOrden tipo = tipoOrdenRepository.findByCodigo(command.getTipoCodigo())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de orden no encontrado: " + command.getTipoCodigo()));
            orden.setTipoId(tipo.getId());
            camposEditados.put("tipoCodigo", command.getTipoCodigo());
            changed = true;
        } else if (command.getTipoCodigo() != null) {
            throw new BadRequestException("tipoCodigo no puede estar vacio");
        }

        if (hasText(command.getPrioridad())) {
            String prioridad = command.getPrioridad().trim().toLowerCase();
            try {
                PrioridadOrdenEnum.valueOf(prioridad);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Prioridad invalida: " + command.getPrioridad());
            }
            orden.setPrioridad(prioridad);
            camposEditados.put("prioridad", prioridad);
            changed = true;
        } else if (command.getPrioridad() != null) {
            throw new BadRequestException("prioridad no puede estar vacia");
        }

        if (command.getMecanicoId() != null) {
            if (!usuarioRepository.existsActiveMecanicoById(command.getMecanicoId())) {
                throw new ResourceNotFoundException("Mecanico no encontrado: " + command.getMecanicoId());
            }
            orden.setMecanicoId(command.getMecanicoId());
            camposEditados.put("mecanicoId", command.getMecanicoId().toString());
            changed = true;
        }

        if (hasItems(command.getProductosAgregar())) {
            validarOrdenPermiteModificarProductos(orden);
            agregarProductosAOrden(orden, command.getProductosAgregar(), now);
            changed = true;
        }

        if (hasItems(command.getProductosActualizar())) {
            validarOrdenPermiteModificarProductos(orden);
            actualizarProductosAOrden(orden, command.getProductosActualizar());
            changed = true;
        }

        if (hasItems(command.getProductosEliminar())) {
            validarOrdenPermiteModificarProductos(orden);
            eliminarProductosAOrden(orden, command.getProductosEliminar());
            changed = true;
        }

        if (hasItems(command.getServiciosAgregar())) {
            validarOrdenPermiteModificarServicios(orden);
            agregarServiciosAOrden(orden, command.getServiciosAgregar(), now);
            changed = true;
        }

        if (hasItems(command.getServiciosActualizar())) {
            validarOrdenPermiteModificarServicios(orden);
            actualizarServiciosAOrden(orden, command.getServiciosActualizar());
            changed = true;
        }

        if (hasItems(command.getServiciosEliminar())) {
            validarOrdenPermiteModificarServicios(orden);
            eliminarServiciosAOrden(orden, command.getServiciosEliminar());
            changed = true;
        }

        if (changed) {
            orden.setUpdatedAt(now);
            ordenRepository.save(orden);
        }

        if (!camposEditados.isEmpty()) {
            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.ORDEN_EDITADA,
                    "orden",
                    null,
                    camposEditados);
        }

        return obtenerDetalle(id);
    }

    /**
     * Lista el historial de auditoría de una orden, resuelta dentro del contexto de taller/sucursal actual.
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<OrdenHistorialResult> listarHistorial(String id) {
        Orden orden = buscarOrdenParaMutacion(id);
        return ordenHistorialService.listar(orden.getId());
    }

    @TenantOperation
    @Transactional
    public List<OrdenProductoResult> agregarProductos(String id, List<OrdenProductoAddCommand> items) {
        Orden orden = buscarOrdenParaMutacion(id);
        validarOrdenPermiteModificarProductos(orden);
        OffsetDateTime now = OffsetDateTime.now();
        return agregarProductosAOrden(orden, items, now);
    }

    public List<OrdenProductoResult> agregarProductos(UUID ordenId, List<OrdenProductoAddCommand> items) {
        Orden orden = buscarOrdenPorIdParaMutacion(ordenId);
        validarOrdenPermiteModificarProductos(orden);
        return agregarProductosAOrden(orden, items, OffsetDateTime.now());
    }

    private List<OrdenProductoResult> agregarProductosAOrden(Orden orden, List<OrdenProductoAddCommand> items, OffsetDateTime now) {
        Map<UUID, OrdenProducto> lineItemsByProducto = new LinkedHashMap<>();
        Set<UUID> productosEnRequest = new HashSet<>();
        UUID usuarioId = UsuarioContext.getCurrentUser();

        for (OrdenProductoAddCommand item : items) {
            if (item.getProductoId() == null) {
                throw new BadRequestException("productoId es requerido");
            }
            if (!productosEnRequest.add(item.getProductoId())) {
                throw new BadRequestException("Producto duplicado en la solicitud: " + item.getProductoId());
            }
            validarCantidad(item.getCantidad());
            Producto producto = buscarProductoDisponible(item.getProductoId(), orden.getSucursalId());
            boolean proporcionadoPorCliente = Boolean.TRUE.equals(item.getProporcionadoPorCliente());
            validarStockSuficiente(producto, item.getCantidad(), proporcionadoPorCliente);

            OrdenProducto lineItem = lineItemsByProducto.computeIfAbsent(item.getProductoId(), productoId ->
                    ordenProductoRepository.findByOrdenIdAndProductoId(orden.getId(), productoId)
                            .orElseGet(() -> OrdenProducto.builder()
                                    .ordenId(orden.getId())
                                    .productoId(productoId)
                                    .usuarioId(usuarioId)
                                    .cantidad(0)
                                    .precioCostoSnapshot(producto.getPrecioCosto())
                                    .precioVentaSnapshot(producto.getPrecioVenta())
                                    .precioAplicado(producto.getPrecioVenta())
                                    .proporcionadoPorCliente(false)
                                    .createdAt(now)
                                    .build())
            );

            int nuevaCantidad = lineItem.getCantidad() + item.getCantidad();
            validarStockSuficiente(producto, nuevaCantidad, proporcionadoPorCliente);
            lineItem.setCantidad(nuevaCantidad);
            lineItem.setProporcionadoPorCliente(proporcionadoPorCliente);
            if (item.getNotas() != null) {
                lineItem.setNotas(item.getNotas());
            }
            if (!proporcionadoPorCliente && producto.getStock() != null) {
                producto.setStock(producto.getStock() - item.getCantidad());
                producto.setUpdatedAt(now);
                productoRepository.save(producto);
            }

            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.PRODUCTO_AGREGADO,
                    "producto",
                    item.getProductoId(),
                    java.util.Map.of(
                            "nombre", producto.getNombre() == null ? "" : producto.getNombre(),
                            "cantidad", item.getCantidad(),
                            "proporcionadoPorCliente", proporcionadoPorCliente
                    ));
        }

        List<OrdenProducto> lineItems = new ArrayList<>(lineItemsByProducto.values());

        List<OrdenProducto> saved = ordenProductoRepository.saveAll(lineItems);
        List<UUID> ids = saved.stream().map(OrdenProducto::getId).toList();
        Map<UUID, OrdenProductoResult> resultsById = ordenProductoRepository.findResultByIdIn(ids).stream()
                .collect(Collectors.toMap(OrdenProductoResult::id, Function.identity()));
        return ids.stream().map(resultsById::get).toList();
    }

    private void actualizarProductosAOrden(Orden orden, List<OrdenProductoUpdateCommand> items) {
        List<OrdenProducto> lineItems = items.stream()
                .map(item -> {
                    if (item.getId() == null) {
                        throw new BadRequestException("id de producto asociado es requerido");
                    }
                    OrdenProducto lineItem = ordenProductoRepository.findByIdAndOrdenId(item.getId(), orden.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Producto asociado no encontrado: " + item.getId()
                            ));
                    Producto producto = buscarProductoDisponible(lineItem.getProductoId(), orden.getSucursalId());
                    Integer cantidad = item.getCantidad() != null ? item.getCantidad() : lineItem.getCantidad();
                    validarCantidad(cantidad);
                    boolean proporcionadoPorCliente = item.getProporcionadoPorCliente() != null
                            ? item.getProporcionadoPorCliente()
                            : Boolean.TRUE.equals(lineItem.getProporcionadoPorCliente());
                    validarStockSuficiente(producto, cantidad, proporcionadoPorCliente);

                    lineItem.setCantidad(cantidad);
                    lineItem.setProporcionadoPorCliente(proporcionadoPorCliente);
                    if (item.getNotas() != null) {
                        lineItem.setNotas(item.getNotas());
                    }
                    ordenHistorialService.registrar(
                            orden.getId(),
                            AccionHistorialEnum.PRODUCTO_MODIFICADO,
                            "producto",
                            lineItem.getProductoId(),
                            java.util.Map.of(
                                    "lineItemId", item.getId().toString(),
                                    "cantidad", cantidad,
                                    "proporcionadoPorCliente", proporcionadoPorCliente
                            ));
                    return lineItem;
                })
                .toList();

        ordenProductoRepository.saveAll(lineItems);
    }

    private void eliminarProductosAOrden(Orden orden, List<UUID> lineItemIds) {
        for (UUID lineItemId : lineItemIds) {
            if (lineItemId == null) {
                throw new BadRequestException("id de producto asociado es requerido");
            }
            int deleted = ordenProductoRepository.deleteByIdAndOrdenId(lineItemId, orden.getId());
            if (deleted == 0) {
                throw new ResourceNotFoundException("Producto asociado no encontrado: " + lineItemId);
            }
            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.PRODUCTO_QUITADO,
                    "producto",
                    null,
                    java.util.Map.of("lineItemId", lineItemId.toString()));
        }
    }

    @TenantOperation
    @Transactional
    public List<OrdenServicioResult> agregarServicios(String id, List<OrdenServicioAddCommand> items) {
        Orden orden = buscarOrdenParaMutacion(id);
        validarOrdenPermiteModificarServicios(orden);
        OffsetDateTime now = OffsetDateTime.now();
        return agregarServiciosAOrden(orden, items, now);
    }

    public List<OrdenServicioResult> agregarServicios(UUID ordenId, List<OrdenServicioAddCommand> items) {
        Orden orden = buscarOrdenPorIdParaMutacion(ordenId);
        validarOrdenPermiteModificarServicios(orden);
        return agregarServiciosAOrden(orden, items, OffsetDateTime.now());
    }

    private List<OrdenServicioResult> agregarServiciosAOrden(Orden orden, List<OrdenServicioAddCommand> items, OffsetDateTime now) {
        Set<UUID> serviciosEnRequest = new HashSet<>();
        UUID usuarioId = UsuarioContext.getCurrentUser();
        List<OrdenServicio> lineItems = items.stream()
                .map(item -> {
                    if (item.getServicioId() == null) {
                        throw new BadRequestException("servicioId es requerido");
                    }
                    if (!serviciosEnRequest.add(item.getServicioId())) {
                        throw new ConflictException("Servicio ya asociado a la orden: " + item.getServicioId());
                    }
                    if (ordenServicioRepository.findByOrdenIdAndServicioId(orden.getId(), item.getServicioId()).isPresent()) {
                        throw new ConflictException("Servicio ya asociado a la orden: " + item.getServicioId());
                    }

                    Servicio servicio = buscarServicioDisponible(item.getServicioId(), orden.getTallerId());
                    BigDecimal precioBase = resolverPrecioServicio(orden, item.getServicioId(), servicio);

                    ordenHistorialService.registrar(
                            orden.getId(),
                            AccionHistorialEnum.SERVICIO_AGREGADO,
                            "servicio",
                            item.getServicioId(),
                            java.util.Map.of(
                                    "nombre", servicio.getNombre() == null ? "" : servicio.getNombre(),
                                    "precioBase", precioBase.toPlainString()
                            ));

                    return OrdenServicio.builder()
                            .ordenId(orden.getId())
                            .servicioId(item.getServicioId())
                            .usuarioId(usuarioId)
                            .precioBaseSnapshot(precioBase)
                            .precioAplicado(precioBase)
                            .descuentoAplicado(BigDecimal.ZERO)
                            .notas(item.getNotas())
                            .createdAt(now)
                            .build();
                })
                .toList();

        List<OrdenServicio> saved = ordenServicioRepository.saveAll(lineItems);
        List<UUID> ids = saved.stream().map(OrdenServicio::getId).toList();
        Map<UUID, OrdenServicioResult> resultsById = ordenServicioRepository.findResultByIdIn(ids).stream()
                .collect(Collectors.toMap(OrdenServicioResult::id, Function.identity()));
        return ids.stream().map(resultsById::get).toList();
    }

    private void actualizarServiciosAOrden(Orden orden, List<OrdenServicioUpdateCommand> items) {
        List<OrdenServicio> lineItems = items.stream()
                .map(item -> {
                    if (item.getId() == null) {
                        throw new BadRequestException("id de servicio asociado es requerido");
                    }
                    OrdenServicio lineItem = ordenServicioRepository.findByIdAndOrdenId(item.getId(), orden.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Servicio asociado no encontrado: " + item.getId()
                            ));

                    if (item.getPrecioAplicado() != null) {
                        validarMontoNoNegativo(item.getPrecioAplicado(), "precioAplicado");
                        lineItem.setPrecioAplicado(item.getPrecioAplicado());
                    }
                    if (item.getDescuentoAplicado() != null) {
                        validarMontoNoNegativo(item.getDescuentoAplicado(), "descuentoAplicado");
                        lineItem.setDescuentoAplicado(item.getDescuentoAplicado());
                    }
                    if (item.getNotas() != null) {
                        lineItem.setNotas(item.getNotas());
                    }
                    ordenHistorialService.registrar(
                            orden.getId(),
                            AccionHistorialEnum.SERVICIO_MODIFICADO,
                            "servicio",
                            lineItem.getServicioId(),
                            java.util.Map.of(
                                    "lineItemId", item.getId().toString(),
                                    "precioAplicado", lineItem.getPrecioAplicado().toPlainString(),
                                    "descuentoAplicado", lineItem.getDescuentoAplicado().toPlainString()
                            ));
                    return lineItem;
                })
                .toList();

        ordenServicioRepository.saveAll(lineItems);
    }

    private void eliminarServiciosAOrden(Orden orden, List<UUID> lineItemIds) {
        for (UUID lineItemId : lineItemIds) {
            if (lineItemId == null) {
                throw new BadRequestException("id de servicio asociado es requerido");
            }
            int deleted = ordenServicioRepository.deleteByIdAndOrdenId(lineItemId, orden.getId());
            if (deleted == 0) {
                throw new ResourceNotFoundException("Servicio asociado no encontrado: " + lineItemId);
            }
            ordenHistorialService.registrar(
                    orden.getId(),
                    AccionHistorialEnum.SERVICIO_QUITADO,
                    "servicio",
                    null,
                    java.util.Map.of("lineItemId", lineItemId.toString()));
        }
    }

    private OrdenCreateResult crearEnSucursal(OrdenCreateCommand command, UUID tallerId, UUID sucursalId) {
        UUID clienteId = resolverCliente(command, tallerId);
        UUID bicicletaId = resolverBicicleta(command, clienteId, tallerId);

        EstadoOrden estado = estadoOrdenRepository.findByCodigo("recibida")
                .orElseThrow(() -> new IllegalStateException("Estado 'recibida' no configurado"));
        TipoOrden tipo = tipoOrdenRepository.findById(command.getTipoTrabajo())
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de orden no encontrado: " + command.getTipoTrabajo()));

        String numeroOrden = secuenciaService.generarNumeroOrden(tallerId);

        OffsetDateTime now = OffsetDateTime.now();
        Orden orden = Orden.builder()
                .tallerId(tallerId)
                .sucursalId(sucursalId)
                .bicicletaId(bicicletaId)
                .mecanicoId(command.getMecanicoId())
                .estadoId(estado.getId())
                .tipoId(tipo.getId())
                .numeroOrden(numeroOrden)
                .diagnosticoInicial(command.getDiagnosticoInicial())
                .observacionesCliente(command.getObservacionesCliente())
                .prioridad(command.getPrioridad() != null ? command.getPrioridad().name() : null)
                .fechaIngreso(now)
                .fechaPrometida(command.getFechaPrometida() != null
                        ? command.getFechaPrometida().atStartOfDay(ZoneOffset.UTC).toOffsetDateTime()
                        : null)
                .descuentoManual(BigDecimal.ZERO)
                .porcentajeDescuentoMembresia(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();
        orden = ordenRepository.save(orden);
        UUID ordenId = orden.getId();

        if (command.getServicios() != null) {
            for (UUID servicioId : command.getServicios()) {
                Servicio servicio = servicioRepository.findById(servicioId)
                        .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + servicioId));
                if (!servicio.getTallerId().equals(tallerId)) {
                    throw new ResourceNotFoundException("Servicio no encontrado: " + servicioId);
                }
                ordenServicioRepository.save(OrdenServicio.builder()
                        .ordenId(ordenId)
                        .servicioId(servicioId)
                        .usuarioId(UsuarioContext.getCurrentUser())
                        .precioBaseSnapshot(servicio.getPrecioBase())
                        .precioAplicado(servicio.getPrecioBase())
                        .descuentoAplicado(BigDecimal.ZERO)
                        .createdAt(now)
                        .build());
            }
        }

        if (command.getProductos() != null) {
            for (OrdenCreateCommand.ProductoItem item : command.getProductos()) {
                Producto producto = productoRepository.findByIdAndSucursalId(item.getProductoId(), sucursalId)
                        .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductoId()));
                ordenProductoRepository.save(OrdenProducto.builder()
                        .ordenId(ordenId)
                        .productoId(item.getProductoId())
                        .usuarioId(UsuarioContext.getCurrentUser())
                        .cantidad(item.getCantidad())
                        .precioCostoSnapshot(producto.getPrecioCosto())
                        .precioVentaSnapshot(producto.getPrecioVenta())
                        .precioAplicado(producto.getPrecioVenta())
                        .proporcionadoPorCliente(false)
                        .createdAt(now)
                        .build());
            }
        }

        return new OrdenCreateResult(ordenId, numeroOrden);
    }

    private UUID resolverSucursal(UUID tallerId, UUID contextSucursalId, UUID requestedSucursalId) {
        if (contextSucursalId != null) {
            if (requestedSucursalId != null && !contextSucursalId.equals(requestedSucursalId)) {
                throw new BadRequestException("La sucursal solicitada no coincide con el contexto actual");
            }
            validarSucursalDelTaller(contextSucursalId, tallerId);
            return contextSucursalId;
        }

        if (requestedSucursalId == null) {
            throw new BadRequestException("Se requiere sucursalId para crear una orden desde contexto de taller");
        }

        validarSucursalDelTaller(requestedSucursalId, tallerId);
        return requestedSucursalId;
    }

    private void validarSucursalDelTaller(UUID sucursalId, UUID tallerId) {
        if (!sucursalRepository.existsByIdAndTallerId(sucursalId, tallerId)) {
            throw new ResourceNotFoundException("Sucursal no pertenece al taller actual");
        }
    }

    private UUID resolverCliente(OrdenCreateCommand command, UUID tallerId) {
        if (command.getClienteId() != null) {
            if (!clienteRepository.existsByIdAndTallerId(command.getClienteId(), tallerId)) {
                throw new ResourceNotFoundException("Cliente no encontrado");
            }
            return command.getClienteId();
        }
        if (command.getClienteNuevo() != null) {
            ClienteCreateCommand cmd = command.getClienteNuevo();
            return clienteService.crear(cmd).getId();
        }
        throw new BadRequestException("Se requiere clienteId o clienteNuevo");
    }

    private UUID resolverBicicleta(OrdenCreateCommand command, UUID clienteId, UUID tallerId) {
        if (command.getBicicletaId() != null) {
            Bicicleta bicicleta = bicicletaRepository.findById(command.getBicicletaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bicicleta no encontrada"));
            if (!clienteRepository.existsByIdAndTallerId(bicicleta.getClienteId(), tallerId)) {
                throw new ResourceNotFoundException("Bicicleta no encontrada");
            }
            if (!bicicleta.getClienteId().equals(clienteId)) {
                throw new BadRequestException("La bicicleta no pertenece al cliente");
            }
            return bicicleta.getId();
        }
        if (command.getBicicletaNueva() != null) {
            BicicletaCreateCommand cmd = command.getBicicletaNueva();
            return bicicletaService.crear(clienteId, cmd).getId();
        }
        throw new BadRequestException("Se requiere bicicletaId o bicicletaNueva");
    }

    private Optional<OrdenDetalleBaseResult> buscarDetalleBaseEnSucursal(String id, UUID sucursalId) {
        return parseUuid(id)
                .flatMap(uuid -> ordenRepository.findDetalleBaseByIdAndSucursalId(uuid, sucursalId))
                .or(() -> ordenRepository.findDetalleBaseByNumeroOrdenAndSucursalId(id, sucursalId));
    }

    private Optional<OrdenDetalleBaseResult> buscarDetalleBaseEnTaller(String id, UUID tallerId) {
        return parseUuid(id)
                .flatMap(uuid -> ordenRepository.findDetalleBaseByIdAndTallerId(uuid, tallerId))
                .or(() -> ordenRepository.findDetalleBaseByNumeroOrdenAndTallerId(id, tallerId));
    }

    /**
     * Busca una orden por su ID o número de orden en el contexto de taller.
     * @param id
     * @param tallerId
     * @return
     */
    private Optional<OrdenReadResult> buscarEnTaller(String id, UUID tallerId) {
        Optional<OrdenReadResult> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findReadByIdAndTallerId(uuid, tallerId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findReadByNumeroOrdenAndTallerId(id, tallerId);
    }

    /**
     * Busca una orden por su ID o número de orden en el contexto de sucursal.
     * @param id
     * @param sucursalId
     * @return
     */
    private Optional<OrdenReadResult> buscarEnSucursal(String id, UUID sucursalId) {
        Optional<OrdenReadResult> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findReadByIdAndSucursalId(uuid, sucursalId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findReadByNumeroOrdenAndSucursalId(id, sucursalId);
    }

    private Orden buscarOrdenParaMutacion(String id) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId != null) {
            return buscarOrdenEntityEnSucursal(id, sucursalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        }

        UUID tallerId = TallerContext.getCurrentTaller();
        if (tallerId != null) {
            return buscarOrdenEntityEnTaller(id, tallerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        }

        throw new IllegalStateException("Contexto de taller o sucursal requerido");
    }

    private Optional<Orden> buscarOrdenEntityEnTaller(String id, UUID tallerId) {
        Optional<Orden> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findByIdAndTallerId(uuid, tallerId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findByNumeroOrdenAndTallerId(id, tallerId);
    }

    private Optional<Orden> buscarOrdenEntityEnSucursal(String id, UUID sucursalId) {
        Optional<Orden> byUuid = parseUuid(id)
                .flatMap(uuid -> ordenRepository.findByIdAndSucursalId(uuid, sucursalId));
        if (byUuid.isPresent()) {
            return byUuid;
        }
        return ordenRepository.findByNumeroOrdenAndSucursalId(id, sucursalId);
    }

    private Orden buscarOrdenPorIdParaMutacion(UUID ordenId) {
        UUID tallerId = TallerContext.getCurrentTaller();
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (tallerId == null && sucursalId == null) {
            throw new IllegalStateException("Contexto de taller o sucursal requerido");
        }
        if (sucursalId != null) {
            return ordenRepository.findByIdAndSucursalId(ordenId, sucursalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        }
        return ordenRepository.findByIdAndTallerId(ordenId, tallerId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
    }

    /**
     * Intenta convertir el ID proporcionado a un UUID. Si la conversión falla, devuelve un Optional vacío.
     * @param id
     * @return
     */
    private void aplicarCambioEstado(Orden orden, String estadoCodigo, String observacion, OffsetDateTime now, UUID usuarioId) {
        if (!hasText(estadoCodigo)) {
            throw new BadRequestException("codigo no puede estar vacio");
        }
        UUID estadoAnteriorId = orden.getEstadoId();
        Optional<EstadoOrden> estadoAnterior = estadoOrdenRepository.findById(estadoAnteriorId);
        if (estadoAnterior.map(EstadoOrden::getEsFinal).orElse(false)) {
            throw new ConflictException("La orden ya se encuentra finalizada");
        }
        EstadoOrden nuevoEstado = estadoOrdenRepository.findByCodigo(estadoCodigo)
                .orElseThrow(() -> new ResourceNotFoundException("Estado de orden no encontrado: " + estadoCodigo));
        if (estadoAnteriorId.equals(nuevoEstado.getId())) {
            throw new ConflictException("La orden ya se encuentra en el estado solicitado");
        }

        orden.setEstadoId(nuevoEstado.getId());
        if (EstadoOrdenEnum.entregada.name().equals(nuevoEstado.getCodigo()) && orden.getFechaEntrega() == null) {
            orden.setFechaEntrega(now);
        }
        ordenEstadoRepository.save(OrdenEstado.builder()
                .ordenId(orden.getId())
                .usuarioId(usuarioId)
                .estadoAnteriorId(estadoAnteriorId)
                .estadoNuevoId(nuevoEstado.getId())
                .observacion(observacion)
                .createdAt(now)
                .build());

        ordenHistorialService.registrar(
                orden.getId(),
                AccionHistorialEnum.ESTADO_CAMBIADO,
                "orden",
                null,
                java.util.Map.of(
                        "estadoAnterior", estadoAnterior.map(EstadoOrden::getCodigo).orElse("desconocido"),
                        "estadoNuevo", nuevoEstado.getCodigo(),
                        "observacion", observacion == null ? "" : observacion
                ));
    }

    private UUID requerirUsuarioContext() {
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (usuarioId == null) {
            throw new BadRequestException("Contexto de usuario requerido");
        }
        return usuarioId;
    }

    private Producto buscarProductoDisponible(UUID productoId, UUID sucursalId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productoId));
        if (!Boolean.TRUE.equals(producto.getActivo())) {
            throw new ResourceNotFoundException("Producto no encontrado: " + productoId);
        }
        if (!producto.getSucursalId().equals(sucursalId)) {
            throw new ResourceNotFoundException(
                    "Producto " + productoId + " no pertenece a la sucursal de esta orden"
            );
        }
        return producto;
    }

    private Servicio buscarServicioDisponible(UUID servicioId, UUID tallerId) {
        Servicio servicio = servicioRepository.findById(servicioId)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado: " + servicioId));
        if (!Boolean.TRUE.equals(servicio.getActivo())) {
            throw new ResourceNotFoundException("Servicio no encontrado: " + servicioId);
        }
        if (!servicio.getTallerId().equals(tallerId)) {
            throw new ResourceNotFoundException(
                    "Servicio " + servicioId + " no pertenece al taller de esta orden"
            );
        }
        return servicio;
    }

    private BigDecimal resolverPrecioServicio(Orden orden, UUID servicioId, Servicio servicio) {
        return sucursalServicioRepository
                .findBySucursalIdAndServicioId(orden.getSucursalId(), servicioId)
                .map(SucursalServicio::getPrecioPersonalizado)
                .filter(precio -> precio != null)
                .orElse(servicio.getPrecioBase());
    }

    private void validarCantidad(Integer cantidad) {
        if (cantidad == null || cantidad < 1) {
            throw new BadRequestException("Cantidad de producto debe ser mayor o igual a 1");
        }
        if (cantidad > 9999) {
            throw new BadRequestException("Cantidad de producto debe ser menor o igual a 9999");
        }
    }

    private void validarStockSuficiente(Producto producto, Integer cantidad, boolean proporcionadoPorCliente) {
        if (proporcionadoPorCliente || producto.getStock() == null) {
            return;
        }
        if (producto.getStock() < cantidad) {
            throw new ConflictException(
                    "Stock insuficiente para producto " + producto.getId() + "; disponible: " + producto.getStock()
            );
        }
    }

    private void validarMontoNoNegativo(BigDecimal value, String field) {
        if (value.signum() < 0) {
            throw new BadRequestException(field + " debe ser mayor o igual a 0");
        }
    }

    private void validarOrdenPermiteModificarProductos(Orden orden) {
        if (orden.getEstadoId() == null) {
            return;
        }
        estadoOrdenRepository.findById(orden.getEstadoId())
                .map(EstadoOrden::getCodigo)
                .filter(codigo -> EstadoOrdenEnum.entregada.name().equals(codigo)
                        || EstadoOrdenEnum.cancelada.name().equals(codigo))
                .ifPresent(codigo -> {
                    throw new BadRequestException("No se pueden modificar productos de una orden " + codigo);
                });
    }

    private void validarOrdenPermiteModificarServicios(Orden orden) {
        if (orden.getEstadoId() == null) {
            return;
        }
        estadoOrdenRepository.findById(orden.getEstadoId())
                .map(EstadoOrden::getCodigo)
                .filter(codigo -> EstadoOrdenEnum.entregada.name().equals(codigo)
                        || EstadoOrdenEnum.cancelada.name().equals(codigo))
                .ifPresent(codigo -> {
                    throw new BadRequestException("No se pueden modificar servicios de una orden " + codigo);
                });
    }

    public List<TipoOrden> listarTipos() {
        return tipoOrdenRepository.findAll();
    }

    private Optional<UUID> parseUuid(String id) {
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasItems(List<?> items) {
        return items != null && !items.isEmpty();
    }

    private String nombrePrioridad(PrioridadOrdenEnum prioridad) {
        String value = prioridad.name();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private OrdenReadResult enriquecerResumen(OrdenReadResult result) {
        List<String> serviceNames = ordenServicioRepository.findServiceNamesByOrder(result.id());
        return new OrdenReadResult(
                result.id(), result.numeroOrden(), result.tallerId(), result.sucursalId(),
                result.estadoId(), result.estadoCodigo(), result.estadoNombre(),
                result.tipoId(), result.tipoCodigo(), result.tipoNombre(),
                result.fechaIngreso(), result.fechaPrometida(), result.fechaEntrega(),
                result.diagnosticoInicial(), result.diagnosticoFinal(), result.observacionesCliente(),
                result.bicicletaId(), result.bicicletaMarca(), result.bicicletaModelo(),
                result.bicicletaTipo(), result.bicicletaAro(), result.bicicletaColor(),
                result.bicicletaNumeroSerie(), result.bicicletaAnio(), result.bicicletaNotas(),
                result.clienteId(), result.clienteNombre(), result.clienteApellido(),
                result.clienteTelefono(), result.clienteEmail(), result.clienteRut(),
                result.clienteDireccion(), result.mecanicoId(), result.mecanicoNombre(),
                result.mecanicoApellido(), result.prioridad(),
                serviceNames.isEmpty() ? null : serviceNames.getFirst(),
                calcularMontoTotal(result.id())
        );
    }

    private BigDecimal calcularMontoTotal(UUID ordenId) {
        if (!ordenServicioRepository.existsByOrdenId(ordenId)
                && !ordenProductoRepository.existsByOrdenId(ordenId)) {
            return null;
        }
        BigDecimal servicios = Optional.ofNullable(ordenServicioRepository.sumTotalByOrdenId(ordenId))
                .orElse(BigDecimal.ZERO);
        BigDecimal productos = Optional.ofNullable(ordenProductoRepository.sumTotalByOrdenId(ordenId))
                .orElse(BigDecimal.ZERO);
        return servicios.add(productos);
    }

    private long countEstado(List<OrdenReadResult> orders, String codigo) {
        return orders.stream().filter(order -> codigo.equals(order.estadoCodigo())).count();
    }

    private String normalizarOpcional(String value, int maxLength, String field) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new BadRequestException(field + " excede " + maxLength + " caracteres");
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    private EtapaMultimediaEnum parseEtapa(String etapa) {
        if (!hasText(etapa)) {
            return null;
        }
        try {
            return EtapaMultimediaEnum.valueOf(etapa.trim().toLowerCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("etapa invalida");
        }
    }

    private String validarMimePermitido(String mimeType) {
        if (mimeType == null) {
            throw new BadRequestException("tipoArchivo es requerido");
        }
        String normalized = mimeType.trim().toLowerCase();
        return switch (normalized) {
            case "image/jpeg", "image/png", "image/webp",
                 "video/mp4", "video/quicktime", "application/pdf" -> normalized;
            default -> throw new BadRequestException("tipoArchivo no soportado");
        };
    }

    private void validarObjectKey(UUID ordenId, String objectKey, String mimeType) {
        String prefix = "ordenes/" + ordenId + "/";
        String expectedExtension = extensionParaMime(mimeType);
        if (objectKey == null
                || !objectKey.startsWith(prefix)
                || !objectKey.endsWith(expectedExtension)) {
            throw new BadRequestException("objectKey invalido para la orden");
        }
        String filename = objectKey.substring(prefix.length(), objectKey.length() - expectedExtension.length());
        try {
            UUID.fromString(filename);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("objectKey invalido para la orden");
        }
    }

    private String publicUrlPara(String objectKey) {
        String baseUrl = r2Properties.publicBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("cloudflare.r2.public-base-url no configurado");
        }
        return baseUrl.replaceAll("/+$", "") + "/" + objectKey;
    }

    private String extensionParaMime(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            case "video/quicktime" -> ".mov";
            case "application/pdf" -> ".pdf";
            default -> throw new BadRequestException("tipoArchivo no soportado");
        };
    }

    private long maxSizeParaMime(String mimeType) {
        return switch (categoriaParaMime(mimeType)) {
            case imagen -> 10L * 1024 * 1024;
            case video -> 100L * 1024 * 1024;
            case documento -> 20L * 1024 * 1024;
        };
    }

    private MediaTypeInfo validarMedia(byte[] content, String declaredContentType) {
        if (content == null || content.length == 0) {
            throw new BadRequestException("file no puede estar vacio");
        }
        String detected = detectarMime(content);
        if (detected == null) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de archivo no soportado");
        }
        if (!detected.equals(declaredContentType)) {
            throw new BadRequestException("tipoArchivo no coincide con el contenido detectado");
        }
        TipoArchivoEnum categoria = categoriaParaMime(detected);
        long maxSize = maxSizeParaMime(detected);
        if (content.length > maxSize) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Archivo demasiado grande");
        }
        return new MediaTypeInfo(detected, categoria);
    }

    private String detectarMime(byte[] bytes) {
        if (startsWith(bytes, new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff})) return "image/jpeg";
        if (startsWith(bytes, new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a})) return "image/png";
        if (bytes.length >= 12 && ascii(bytes, 0, 4).equals("RIFF") && ascii(bytes, 8, 4).equals("WEBP")) return "image/webp";
        if (bytes.length >= 12 && ascii(bytes, 4, 4).equals("ftyp")) {
            String brand = ascii(bytes, 8, 4);
            return brand.startsWith("qt") ? "video/quicktime" : "video/mp4";
        }
        if (startsWith(bytes, "%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII))) return "application/pdf";
        return null;
    }

    private boolean startsWith(byte[] content, byte[] signature) {
        return content.length >= signature.length
                && Arrays.equals(Arrays.copyOf(content, signature.length), signature);
    }

    private String ascii(byte[] content, int offset, int length) {
        return new String(content, offset, length, java.nio.charset.StandardCharsets.US_ASCII);
    }

    private TipoArchivoEnum categoriaParaMime(String mimeType) {
        if (mimeType.startsWith("image/")) return TipoArchivoEnum.imagen;
        if (mimeType.startsWith("video/")) return TipoArchivoEnum.video;
        return TipoArchivoEnum.documento;
    }

    private record MediaTypeInfo(String mimeType, TipoArchivoEnum categoria) {
    }
}
