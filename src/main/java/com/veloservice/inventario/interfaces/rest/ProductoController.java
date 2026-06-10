package com.veloservice.inventario.interfaces.rest;

import com.veloservice.inventario.application.dto.ProductoResult;
import com.veloservice.inventario.application.usecase.ProductoService;
import com.veloservice.inventario.interfaces.mapper.ProductoMapper;
import com.veloservice.inventario.interfaces.rest.dto.InventarioMetricasResponse;
import com.veloservice.inventario.interfaces.rest.dto.ProductoRequest;
import com.veloservice.inventario.interfaces.rest.dto.ProductoResponse;
import com.veloservice.inventario.interfaces.rest.dto.ProductoStockMinimoResponse;
import com.veloservice.inventario.interfaces.rest.dto.ProductosListResponse;
import com.veloservice.administracion.infraestructure.persistence.repository.SucursalRepository;
import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for products.
 */
@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;
    private final SucursalRepository sucursalRepository;

    /**
     * Creates a product.
     *
     * @param request product request
     * @return created product
     */
    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse response = ProductoMapper.toResponse(
            productoService.crear(ProductoMapper.toCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lists products for the current tenant.
     *
     * @return product list
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(@RequestParam(required = false) String search,
                                                      @RequestParam(required = false) UUID sucursalId) {
        List<ProductoResult> results = (search != null && search.length() >= 1)
                ? productoService.buscar(search, sucursalId)
                : productoService.listar(sucursalId);
        List<ProductoResponse> productos = ProductoMapper.toResponseList(results);
        return ResponseEntity.ok(Map.of(
                "total", productos.size(),
                "productos", productos
        ));
    }

    /**
     * Lists products with low stock.
     *
     * @return product alerts
     */
    @GetMapping("/alertas")
    public ResponseEntity<List<ProductoResponse>> alertas() {
        return ResponseEntity.ok(ProductoMapper.toResponseList(productoService.alertasStockBajo()));
    }

    @GetMapping("/metricas")
    public ResponseEntity<InventarioMetricasResponse> metricas() {
        return ResponseEntity.ok(productoService.metricas());
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportarCsv() {
        List<ProductoResponse> productos = ProductoMapper.toResponseList(productoService.listar());
        StringBuilder csv = new StringBuilder();
        csv.append("id,nombre,sku,categoria,costo_unitario,precio_asignado,stock\n");
        for (ProductoResponse producto : productos) {
            csv.append(producto.getId()).append(',')
                    .append(csvEscape(producto.getNombre())).append(',')
                    .append(csvEscape(producto.getSku())).append(',')
                    .append(csvEscape(producto.getCategoria())).append(',')
                    .append(producto.getPrecioCosto()).append(',')
                    .append(producto.getPrecioVenta()).append(',')
                    .append(producto.getStock())
                    .append('\n');
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=productos.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv.toString());
    }

    @GetMapping("/lista-productos")
    public ResponseEntity<ProductosListResponse> listaProductos(
            @RequestParam(required = false) UUID sucursalId) {

        UUID resolvedSucursalId;
        if (sucursalId != null) {
            if (!sucursalRepository.existsByIdAndTallerId(sucursalId, TallerContext.getCurrentTaller())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            resolvedSucursalId = sucursalId;
        } else {
            resolvedSucursalId = SucursalContext.getCurrentSucursal();
        }

        if (resolvedSucursalId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ProductosListResponse.ProductoListItem> items = productoService.listarBySucursal(resolvedSucursalId)
                .stream()
                .map(p -> new ProductosListResponse.ProductoListItem(p.getId(), p.getNombre(), p.getPrecioVenta(), p.getStock()))
                .toList();
        return ResponseEntity.ok(new ProductosListResponse(items));
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<Map<String, Object>> stockBajo() {
    List<ProductoResponse> productos = ProductoMapper.toResponseList(productoService.alertasStockBajo());
    return ResponseEntity.ok(Map.of(
            "total", productos.size(),
            "productos", productos
    ));
}
}