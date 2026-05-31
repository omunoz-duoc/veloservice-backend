package com.veloservice.servicios.interfaces.rest;

import com.veloservice.servicios.interfaces.rest.dto.ServicioRequest;
import com.veloservice.servicios.interfaces.rest.dto.ServicioResponse;
import com.veloservice.servicios.interfaces.rest.dto.SucursalServicioRequest;
import com.veloservice.servicios.interfaces.rest.dto.SucursalServicioResponse;

import com.veloservice.servicios.application.usecase.ServicioService;
import com.veloservice.servicios.interfaces.mapper.ServicioMapper;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar() {
        List<ServicioResponse> servicios = ServicioMapper.toResponseList(servicioService.listar());
        return ResponseEntity.ok(Map.of(
                "total", servicios.size(),
                "servicios", servicios
        ));
    }

    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        ServicioResponse response = ServicioMapper.toResponse(
                servicioService.crear(ServicioMapper.toCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicioResponse> actualizar(@PathVariable UUID id, @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(ServicioMapper.toResponse(
                servicioService.actualizar(id, ServicioMapper.toCommand(request))
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        servicioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sucursal")
    public ResponseEntity<Void> asignarPrecioSucursal(@Valid @RequestBody SucursalServicioRequest request) {
        servicioService.asignarPrecioSucursal(ServicioMapper.toSucursalCommand(request));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sucursal")
    public ResponseEntity<List<SucursalServicioResponse>> listarSucursal() {
        return ResponseEntity.ok(ServicioMapper.toSucursalResponseList(servicioService.listarServiciosSucursal()));
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportarCsv() {
        List<ServicioResponse> servicios = ServicioMapper.toResponseList(servicioService.listar());
        StringBuilder csv = new StringBuilder();
        csv.append("id,nombre,descripcion,precio_base,activo\n");
        for (ServicioResponse servicio : servicios) {
            csv.append(servicio.getId()).append(',')
                    .append(csvEscape(servicio.getNombre())).append(',')
                    .append(csvEscape(servicio.getDescripcion())).append(',')
                    .append(servicio.getPrecioBase()).append(',')
                    .append(servicio.getActivo())
                    .append('\n');
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=servicios.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv.toString());
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
}