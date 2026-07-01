package com.veloservice.ordenes.domain;

/**
 * Vocabulario de acciones registradas en orden_historial.
 * El valor name() se persiste en la columna accion (VARCHAR).
 */
public enum AccionHistorialEnum {
    ESTADO_CAMBIADO,
    ORDEN_EDITADA,
    PRODUCTO_AGREGADO,
    PRODUCTO_MODIFICADO,
    PRODUCTO_QUITADO,
    SERVICIO_AGREGADO,
    SERVICIO_MODIFICADO,
    SERVICIO_QUITADO,
    MULTIMEDIA_AGREGADA,
    MULTIMEDIA_QUITADA
}
