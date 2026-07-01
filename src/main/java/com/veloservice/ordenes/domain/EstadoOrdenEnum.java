package com.veloservice.ordenes.domain;

public enum EstadoOrdenEnum {
    recibida,
    en_diagnostico,
    esperando_repuestos,
    en_reparacion,
    control_calidad,
    lista_para_entrega,
    entregada,
    cancelada
}