package com.veloservice.clientes.interfaces.rest;

import com.veloservice.clientes.application.usecase.ClienteService;
import com.veloservice.clientes.interfaces.mapper.ClienteMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * Creates a customer for the current tenant.
     */
    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ClienteMapper.toResponse(clienteService.crear(ClienteMapper.toCommand(request)))
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
     * Retrieves a customer by identifier.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(ClienteMapper.toResponse(clienteService.obtener(id)));
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