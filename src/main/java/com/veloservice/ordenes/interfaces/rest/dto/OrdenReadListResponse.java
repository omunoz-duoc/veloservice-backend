package com.veloservice.ordenes.interfaces.rest.dto;

import java.util.List;

public record OrdenReadListResponse(int total, List<OrdenReadResponse> ordenes) {
}
