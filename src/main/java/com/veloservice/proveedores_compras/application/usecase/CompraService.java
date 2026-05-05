package com.veloservice.proveedores_compras.application.usecase;

import com.veloservice.proveedores_compras.application.dto.CompraCreateCommand;
import com.veloservice.proveedores_compras.application.dto.CompraLineaResult;
import com.veloservice.proveedores_compras.application.dto.CompraResult;
import com.veloservice.proveedores_compras.domain.model.Compra;
import com.veloservice.proveedores_compras.domain.model.CompraProducto;
import com.veloservice.proveedores_compras.domain.model.Proveedor;
import com.veloservice.proveedores_compras.domain.model.SucursalProveedor;
import com.veloservice.proveedores_compras.infraestructure.persistence.repository.CompraRepository;
import com.veloservice.proveedores_compras.infraestructure.persistence.repository.ProveedorRepository;
import com.veloservice.proveedores_compras.infraestructure.persistence.repository.SucursalProveedorRepository;
import com.veloservice.config.enums.EstadoCompraEnum;
import com.veloservice.config.enums.TipoMovimientoEnum;
import com.veloservice.inventario.domain.model.MovimientoStock;
import com.veloservice.inventario.domain.model.Producto;
import com.veloservice.inventario.infraestructure.persistence.repository.MovimientoStockRepository;
import com.veloservice.inventario.infraestructure.persistence.repository.ProductoRepository;
import com.veloservice.config.tenant.TenantOperation;
import com.veloservice.config.security.SucursalContext;
import com.veloservice.config.security.UsuarioContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles purchase operations and stock updates.
 */
@Service
@RequiredArgsConstructor
public class CompraService {

    private static final BigDecimal IVA_RATE = BigDecimal.valueOf(0.19);

    private final CompraRepository compraRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final SucursalProveedorRepository sucursalProveedorRepository;
    private final MovimientoStockRepository movimientoRepository;

    /**
     * Creates a purchase in pending state.
     *
     * @param request purchase request
     * @return created purchase
     */
    @TenantOperation
    @Transactional
    public CompraResult crear(CompraCreateCommand command) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de sucursal/usuario requerido");
        }

        SucursalProveedor sucursalProveedor = sucursalProveedorRepository
                .findById(command.getSucursalProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("SucursalProveedor no encontrado"));
        if (!sucursalId.equals(sucursalProveedor.getSucursalId())) {
            throw new IllegalArgumentException("El proveedor no pertenece a la sucursal actual");
        }

        BigDecimal neto = BigDecimal.ZERO;
        for (var linea : command.getLineas()) {
            BigDecimal subtotal = linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()));
            neto = neto.add(subtotal);
        }
        BigDecimal iva = neto.multiply(IVA_RATE);
        BigDecimal total = neto.add(iva);

        Compra compra = Compra.builder()
            .sucursalProveedorId(command.getSucursalProveedorId())
                .usuarioId(usuarioId)
                .numeroFactura(command.getNumeroFactura())
                .neto(neto)
                .iva(iva)
                .total(total)
                .estado(EstadoCompraEnum.borrador)
                .fechaCompra(command.getFechaCompra())
                .notas(command.getNotas())
                .build();

        for (var lineaReq : command.getLineas()) {
            BigDecimal subtotal = lineaReq.getPrecioUnitario().multiply(BigDecimal.valueOf(lineaReq.getCantidad()));
            CompraProducto linea = CompraProducto.builder()
                    .compra(compra)
                    .productoId(lineaReq.getProductoId())
                    .cantidad(lineaReq.getCantidad())
                    .precioUnitario(lineaReq.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();
            compra.getLineas().add(linea);
        }

        compra = compraRepository.save(compra);
        return toResult(compra);
    }

    /**
     * Confirms purchase reception and updates stock.
     *
     * @param compraId purchase identifier
     * @return updated purchase
     */
    @TenantOperation
    @Transactional
    public CompraResult confirmarRecepcion(UUID compraId) {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        UUID usuarioId = UsuarioContext.getCurrentUser();
        if (sucursalId == null || usuarioId == null) {
            throw new IllegalStateException("Contexto de sucursal/usuario requerido");
        }

        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new IllegalArgumentException("Compra no encontrada"));

        SucursalProveedor sucursalProveedor = sucursalProveedorRepository.findById(compra.getSucursalProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor de sucursal no encontrado"));
        if (!sucursalId.equals(sucursalProveedor.getSucursalId())) {
            throw new IllegalArgumentException("La compra no pertenece a la sucursal actual");
        }

        if (!EstadoCompraEnum.borrador.equals(compra.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden recepcionar compras en estado borrador");
        }

        compra.setEstado(EstadoCompraEnum.recibida);
        compra.setFechaRecepcion(LocalDate.now());

        for (CompraProducto linea : compra.getLineas()) {
            Producto producto = productoRepository.findById(linea.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + linea.getProductoId()));
            if (!sucursalId.equals(producto.getSucursalId())) {
                throw new IllegalArgumentException("El producto no pertenece a la sucursal actual");
            }

            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior + linea.getCantidad();
            if (stockNuevo < 0) {
                throw new IllegalStateException("Stock negativo no permitido (RN17)");
            }

            producto.setStock(stockNuevo);
            productoRepository.save(producto);

            MovimientoStock movimiento = MovimientoStock.builder()
                    .productoId(producto.getId())
                    .compraId(compra.getId())
                    .usuarioId(usuarioId)
                    .tipo(TipoMovimientoEnum.entrada)
                    .cantidad(linea.getCantidad())
                    .stockAnterior(stockAnterior)
                    .stockPosterior(stockNuevo)
                    .motivo("Recepcion de compra #" + compra.getNumeroFactura())
                    .build();
            movimientoRepository.save(movimiento);
        }

        compra = compraRepository.save(compra);
        return toResult(compra);
    }

    /**
     * Lists purchases for the current tenant.
     *
     * @return purchases
     */
    @TenantOperation
    @Transactional(readOnly = true)
    public List<CompraResult> listar() {
        UUID sucursalId = SucursalContext.getCurrentSucursal();
        if (sucursalId == null) {
            return List.of();
        }
        return compraRepository.findBySucursalIdOrderByFechaCompraDesc(sucursalId).stream()
                .map(this::toResult)
                .collect(java.util.stream.Collectors.toList());
    }

    private CompraResult toResult(Compra compra) {
        String proveedorNombre = sucursalProveedorRepository.findById(compra.getSucursalProveedorId())
            .flatMap(sp -> proveedorRepository.findById(sp.getProveedorId()))
            .map(Proveedor::getNombre)
            .orElse(null);

        return CompraResult.builder()
                .id(compra.getId())
                .proveedorNombre(proveedorNombre)
                .numeroFactura(compra.getNumeroFactura())
                .neto(compra.getNeto())
                .iva(compra.getIva())
                .total(compra.getTotal())
                .estado(compra.getEstado())
                .fechaCompra(compra.getFechaCompra())
                .lineas(compra.getLineas().stream()
                        .map(linea -> CompraLineaResult.builder()
                                .productoId(linea.getProductoId())
                                .cantidad(linea.getCantidad())
                                .precioUnitario(linea.getPrecioUnitario())
                                .subtotal(linea.getSubtotal())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

}