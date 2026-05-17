package com.veloservice.administracion.interfaces.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MecanicoResponse {
    private String nombre;
    private String apellido;

    @JsonProperty("ordenes_en_curso")
    private List<OrdenEnCursoResponse> ordenesEnCurso;

    @Data
    @Builder
    @AllArgsConstructor
    public static class OrdenEnCursoResponse {
        private String id;
    }
}
