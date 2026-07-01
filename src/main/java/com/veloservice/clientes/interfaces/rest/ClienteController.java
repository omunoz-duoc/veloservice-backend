package com.veloservice.clientes.interfaces.rest;

import com.veloservice.clientes.application.usecase.ClienteService;
import com.veloservice.clientes.application.usecase.BicicletaService;
import com.veloservice.clientes.interfaces.mapper.ClienteMapper;
import com.veloservice.clientes.interfaces.rest.dto.ClienteBusquedaResponse;
import com.veloservice.clientes.interfaces.rest.dto.ClienteDetalleResponse;
import com.veloservice.clientes.interfaces.rest.dto.ClienteRequest;
import com.veloservice.clientes.interfaces.rest.dto.ClienteResponse;
import com.veloservice.clientes.interfaces.rest.dto.ClienteResumenResponse;
import com.veloservice.clientes.interfaces.rest.dto.ClienteListItem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST endpoints for customers.
 */
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final BicicletaService bicicletaService;

    /**
     * Creates a customer for the current tenant.
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ClienteMapper.toResponse(clienteService.crear(ClienteMapper.toCommand(request)))
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(
                ClienteMapper.toResponse(clienteService.actualizar(id, ClienteMapper.toCommand(request)))
        );
    }

    /**
     * Lists customers for the current tenant.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<ClienteResponse> clientes = ClienteMapper.toResponseList(clienteService.listar());
        return ResponseEntity.ok(Map.of(
                "total", clientes.size(),
                "clientes", clientes
        ));
    }

    /**
     * Searches customers by text for the current tenant.
     *
     * @param texto search text
     * @return matching customers
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteBusquedaResponse>> buscar(@org.springframework.web.bind.annotation.RequestParam("q") String texto) {
        return ResponseEntity.ok(List.of()); // TODO: implementar buscar en ClienteService
    }
    
     /**
     * Resumen of customers for a sucursal, including aggregated data.
     * 
     * @return customer resumen list
     */
    @GetMapping("/resumen")
    public ResponseEntity<List<ClienteResumenResponse>> listarResumen() {
        return ResponseEntity.ok(List.of()); // TODO: implementar listarResumen en ClienteService
    }

    @GetMapping("/lista-clientes")
    public ResponseEntity<List<ClienteListItem>> listaClientes() {
        List<ClienteListItem> items = clienteService.listar().stream()
                .map(c -> new ClienteListItem(
                        c.getId(),
                        (c.getNombre() + " " + (c.getApellido() != null ? c.getApellido() : "")).trim(),
                        c.getRut()))
                .toList();
        return ResponseEntity.ok(items);
    }

    /**
     * Retrieves a customer by identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDetalleResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(ClienteMapper.toDetalleResponse(clienteService.obtenerDetalle(id)));
    }

    /**
     * Exports customers as CSV.
     */
    @GetMapping("/exportar-csv")
    public ResponseEntity<byte[]> exportarCsv() {
        List<ClienteResponse> clientes = ClienteMapper.toResponseList(clienteService.listar());

        StringBuilder csv = new StringBuilder();
        csv.append("id,nombre,apellido,tipo,rut,email,telefono,bicicletas_count,ordenes_count,total_gastado\n");

        for (ClienteResponse c : clientes) {
            csv.append(safe(c.getId())).append(",")
               .append(safe(c.getNombre())).append(",")
               .append(safe(c.getApellido())).append(",")
               .append(safe(c.getTipo())).append(",")
               .append(safe(c.getRut())).append(",")
               .append(safe(c.getEmail())).append(",")
               .append(safe(c.getTelefono())).append(",")
               .append(c.getBicicletasCount()).append(",")
               .append(c.getOrdenesCount()).append(",")
               .append(c.getTotalGastado() != null ? c.getTotalGastado() : 0)
               .append("\n");
        }

        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"clientes.csv\"")
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
}
