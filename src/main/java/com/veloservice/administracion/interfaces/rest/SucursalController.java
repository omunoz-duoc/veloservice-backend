package com.veloservice.administracion.interfaces.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.veloservice.administracion.application.usecase.SucursalService;
import com.veloservice.administracion.interfaces.mapper.SucursalMapper;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/sucursales")
@RequiredArgsConstructor
public class SucursalController {
    
    private final SucursalService sucursalService;

    /**
     * Lista todas las sucursales 
     */
    @GetMapping
    public ResponseEntity<List<SucursalRespone>> listar() {
        List<SucursalRespone> sucursales = SucursalMapper.toResponseList(sucursalService.listar());
        return ResponseEntity.ok(sucursales);
    }
    
}
