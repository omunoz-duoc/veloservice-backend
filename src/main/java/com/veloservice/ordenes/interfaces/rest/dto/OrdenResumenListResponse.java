package com.veloservice.ordenes.interfaces.rest.dto;

import java.util.List;

public record OrdenResumenListResponse(int total, List<OrdenResumenResponse> ordenes) {}
