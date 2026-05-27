package com.veloservice.ordenes.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TipoOrdenEnumConverterTest {

    private final TipoOrdenEnumConverter converter = new TipoOrdenEnumConverter();

    @Test
    void convertsLegacyAndCanonicalDbValues() {
        assertEquals(TipoOrdenEnum.MANTENCION, converter.convertToEntityAttribute("mantencion"));
        assertEquals(TipoOrdenEnum.REPARACION, converter.convertToEntityAttribute("REPARACION"));
        assertEquals(TipoOrdenEnum.REVISION, converter.convertToEntityAttribute("revision"));
        assertEquals(TipoOrdenEnum.GARANTIA, converter.convertToEntityAttribute("garantia"));
    }

    @Test
    void writesNormalizedDbValues() {
        assertEquals("mantencion", converter.convertToDatabaseColumn(TipoOrdenEnum.MANTENCION));
        assertEquals("reparacion", converter.convertToDatabaseColumn(TipoOrdenEnum.REPARACION));
        assertEquals("garantia", converter.convertToDatabaseColumn(TipoOrdenEnum.GARANTIA));
    }
}