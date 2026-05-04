package com.veloservice.finanzas.internal.service;

import com.veloservice.finanzas.api.CobroRequest;
import com.veloservice.config.enums.EstadoCobroEnum;
import com.veloservice.config.enums.EstadoOrdenEnum;
import com.veloservice.finanzas.internal.entity.Cobro;
import com.veloservice.taller.internal.entity.Orden;
import com.veloservice.taller.internal.entity.OrdenProducto;
import com.veloservice.taller.internal.entity.OrdenServicio;
import com.veloservice.crm.internal.entity.SucursalCliente;
import com.veloservice.finanzas.internal.repository.CobroRepository;
import com.veloservice.crm.internal.repository.MembresiaRepository;
import com.veloservice.taller.internal.repository.OrdenProductoRepository;
import com.veloservice.taller.internal.repository.OrdenRepository;
import com.veloservice.taller.internal.repository.OrdenServicioRepository;
import com.veloservice.crm.internal.repository.BicicletaRepository;
import com.veloservice.crm.internal.repository.SucursalClienteRepository;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.UsuarioContext;
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
    public Cobro liquidar(CobroRequest request) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) throw new IllegalStateException("Contexto requerido");

        Orden orden = ordenRepository.findByIdAndSucursalId(request.getOrdenId(), sucursalId)
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
                .map(OrdenProducto::getPrecioAplicado)
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

        BigDecimal descuentoManual = request.getDescuentoManual() == null ? BigDecimal.ZERO : request.getDescuentoManual();

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
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .subtotalServicios(subtotalServicios.setScale(2, RoundingMode.HALF_UP))
                .subtotalProductos(subtotalProductos.setScale(2, RoundingMode.HALF_UP))
                .descuentoMembresia(descuentoMembresia.setScale(2, RoundingMode.HALF_UP))
                .descuentoManual(descuentoManual.setScale(2, RoundingMode.HALF_UP))
                .neto(neto.setScale(2, RoundingMode.HALF_UP))
                .iva(iva)
                .total(total)
                .metodoPago(request.getMetodoPago())
                .estado(EstadoCobroEnum.pagado)
                .fechaPago(OffsetDateTime.now())
                .build();

        return cobroRepository.save(cobro);
    }

    @Transactional(readOnly = true)
    public List<Cobro> listar() {
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
}