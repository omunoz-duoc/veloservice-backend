package com.veloservice.config.enums;

import java.util.Arrays;

public enum TipoOrdenEnum {
    MANTENCION("mantencion"),
    REPARACION("reparacion"),
    REVISION("revision"),
    GARANTIA("garantia"),
    PERSONALIZACION("personalizacion");

    private final String dbValue;

    TipoOrdenEnum(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static TipoOrdenEnum fromDbValue(String value) {
        if (value == null) {
            return null;
        }

        return Arrays.stream(values())
                .filter(tipoOrden -> tipoOrden.name().equalsIgnoreCase(value)
                        || tipoOrden.dbValue.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown TipoOrdenEnum value: " + value));
    }
}