package com.veloservice.finanzas.application.usecase;

import com.veloservice.finanzas.application.dto.CobroCreateCommand;
import com.veloservice.finanzas.application.dto.CobroResult;
import com.veloservice.finanzas.application.dto.FinanzasHoyResult;
import com.veloservice.finanzas.domain.model.Cobro;
import com.veloservice.finanzas.infraestructure.persistence.repository.CobroRepository;
import com.veloservice.ordenes.domain.model.Orden;
import com.veloservice.ordenes.domain.model.OrdenProducto;
import com.veloservice.ordenes.domain.model.OrdenServicio;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenProductoRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenRepository;
import com.veloservice.ordenes.infraestructure.persistence.repository.OrdenServicioRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.BicicletaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.MembresiaRepository;
import com.veloservice.clientes.infraestructure.persistence.repository.SucursalClienteRepository;
import com.veloservice.finanzas.domain.EstadoCobroEnum;
import com.veloservice.ordenes.domain.EstadoOrdenEnum;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.UsuarioContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;

@Service
@RequiredArgsConstructor
public class CobroService {

    private final CobroRepository cobroRepository;
    private final OrdenRepository ordenRepository;
    private final OrdenServicioRepository ordenServicioRepository;
    private final OrdenProductoRepository ordenProductoRepository;
    private final BicicletaRepository bicicletaRepository;
    private final SucursalClienteRepository sucursalClienteRepository;
    private final MembresiaRepository membresiaRepository;

    @Transactional
    public CobroResult liquidar(CobroCreateCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) throw new IllegalStateException("Contexto requerido");

        Orden orden = ordenRepository.findByIdAndSucursalId(command.getOrdenId(), sucursalId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada"));

        if (!EstadoOrdenEnum.entregada.equals(orden.getEstado())) {
            throw new IllegalArgumentException("Solo se puede liquidar una orden entregada");
        }

        if (cobroRepository.findByOrdenId(orden.getId()).isPresent()) {
            throw new IllegalArgumentException("La orden ya tiene un cobro registrado");
        }

        List<OrdenServicio> servicios = ordenServicioRepository.findByOrdenId(orden.getId());
        List<OrdenProducto> productos = ordenProductoRepository.findByOrdenId(orden.getId());

        BigDecimal subtotalServicios = servicios.stream()
                .map(OrdenServicio::getPrecioAplicado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotalProductos = productos.stream()
                .map(producto -> producto.getPrecioAplicado().multiply(BigDecimal.valueOf(producto.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal descuentoMembresia = BigDecimal.ZERO;

        // intentar obtener cliente desde la bicicleta
        var bicicletaOpt = bicicletaRepository.findById(orden.getBicicletaId());
        if (bicicletaOpt.isPresent()) {
            var bicicleta = bicicletaOpt.get();
            var cliente = bicicleta.getCliente();
            if (cliente != null) {
                var vinculo = sucursalClienteRepository.findBySucursalIdAndClienteId(sucursalId, cliente.getId());
                if (vinculo.isPresent() && vinculo.get().getMembresiaId() != null) {
                    var membOpt = membresiaRepository.findById(vinculo.get().getMembresiaId());
                    if (membOpt.isPresent()) {
                        var porcentaje = membOpt.get().getPorcentajeDescuento();
                        var base = subtotalServicios.add(subtotalProductos);
                        descuentoMembresia = base.multiply(porcentaje).divide(new BigDecimal("100"));
                    }
                }
            }
        }

        BigDecimal descuentoManual = command.getDescuentoManual() == null ? BigDecimal.ZERO : command.getDescuentoManual();

        BigDecimal baseTotal = subtotalServicios.add(subtotalProductos);
        // RN16: descuentos no superan subtotal
        if (descuentoMembresia.add(descuentoManual).compareTo(baseTotal) > 0) {
            throw new IllegalArgumentException("Descuentos no pueden superar el subtotal (RN16)");
        }

        BigDecimal neto = baseTotal.subtract(descuentoMembresia).subtract(descuentoManual);
        if (neto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Neto no puede ser negativo (RN16)");
        }

        BigDecimal iva = neto.multiply(new BigDecimal("0.19")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = neto.add(iva).setScale(2, RoundingMode.HALF_UP);

        Cobro cobro = Cobro.builder()
                .ordenId(orden.getId())
                .usuarioId(usuarioId)
            .tipoDocumento(command.getTipoDocumento())
            .numeroDocumento(command.getNumeroDocumento())
                .subtotalServicios(subtotalServicios.setScale(2, RoundingMode.HALF_UP))
                .subtotalProductos(subtotalProductos.setScale(2, RoundingMode.HALF_UP))
                .descuentoMembresia(descuentoMembresia.setScale(2, RoundingMode.HALF_UP))
                .descuentoManual(descuentoManual.setScale(2, RoundingMode.HALF_UP))
                .neto(neto.setScale(2, RoundingMode.HALF_UP))
                .iva(iva)
                .total(total)
            .metodoPago(command.getMetodoPago())
                .estado(EstadoCobroEnum.pagado)
                .fechaPago(OffsetDateTime.now())
                .build();

        return toResult(cobroRepository.save(cobro));
    }

    @Transactional(readOnly = true)
    public List<CobroResult> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        Set<UUID> ordenesSucursal = ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId)
                .stream()
                .map(Orden::getId)
                .collect(Collectors.toSet());
        return cobroRepository.findAll().stream()
                .filter(c -> ordenesSucursal.contains(c.getOrdenId()))
            .map(this::toResult)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BigDecimal ingresosHoy() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return BigDecimal.ZERO;
        }
        Set<UUID> ordenesSucursal = ordenRepository.findAllBySucursalIdOrderByFechaIngresoDesc(sucursalId)
                .stream()
                .map(Orden::getId)
                .collect(Collectors.toSet());
        LocalDate hoy = LocalDate.now();
        return cobroRepository.findAll().stream()
                .filter(c -> ordenesSucursal.contains(c.getOrdenId()))
                .filter(c -> EstadoCobroEnum.pagado.equals(c.getEstado()))
                .filter(c -> c.getFechaPago() != null && hoy.equals(c.getFechaPago().toLocalDate()))
                .map(Cobro::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public FinanzasHoyResult finanzasHoy() {
        OffsetDateTime ahora = OffsetDateTime.now();
        OffsetDateTime inicioDia = ahora.toLocalDate().atStartOfDay().atOffset(ahora.getOffset());
        OffsetDateTime finDia = inicioDia.plusDays(1);
        OffsetDateTime inicioAyer = inicioDia.minusDays(1);

        BigDecimal totalHoy = cobroRepository.sumTotalByCreatedAtBetween(inicioDia, finDia);
        BigDecimal totalAyer = cobroRepository.sumTotalByCreatedAtBetween(inicioAyer, inicioDia);

        BigDecimal delta = null;
        if (totalAyer.compareTo(BigDecimal.ZERO) != 0) {
            delta = totalHoy.subtract(totalAyer)
                    .divide(totalAyer, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new FinanzasHoyResult(totalHoy, delta);
    }

    private CobroResult toResult(Cobro cobro) {
        return CobroResult.builder()
                .id(cobro.getId())
                .ordenId(cobro.getOrdenId())
                .usuarioId(cobro.getUsuarioId())
                .tipoDocumento(cobro.getTipoDocumento())
                .numeroDocumento(cobro.getNumeroDocumento())
                .subtotalServicios(cobro.getSubtotalServicios())
                .subtotalProductos(cobro.getSubtotalProductos())
                .descuentoMembresia(cobro.getDescuentoMembresia())
                .descuentoManual(cobro.getDescuentoManual())
                .neto(cobro.getNeto())
                .iva(cobro.getIva())
                .total(cobro.getTotal())
                .metodoPago(cobro.getMetodoPago())
                .estado(cobro.getEstado())
                .folioSii(cobro.getFolioSii())
                .estadoSii(cobro.getEstadoSii())
                .fechaPago(cobro.getFechaPago())
                .anuladaAt(cobro.getAnuladaAt())
                .motivoAnulacion(cobro.getMotivoAnulacion())
                .createdAt(cobro.getCreatedAt())
                .build();
    }
}
