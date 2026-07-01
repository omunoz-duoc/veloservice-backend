package com.veloservice.clientes.interfaces.rest.dto;

import java.util.UUID;

public record ClienteListItem(UUID id, String nombre, String rut) {}
