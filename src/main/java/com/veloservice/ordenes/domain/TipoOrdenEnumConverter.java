package com.veloservice.ordenes.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TipoOrdenEnumConverter implements AttributeConverter<TipoOrdenEnum, String> {

    @Override
    public String convertToDatabaseColumn(TipoOrdenEnum attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public TipoOrdenEnum convertToEntityAttribute(String dbData) {
        return TipoOrdenEnum.fromDbValue(dbData);
    }
}