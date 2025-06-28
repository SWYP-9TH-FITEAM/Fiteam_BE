package com.backend.Fiteam.ConfigEnum;

import jakarta.persistence.AttributeConverter;

import java.util.Arrays;

public abstract class EnumCodeConverter<T extends Enum<T> & EnumType> implements AttributeConverter<T, Integer> {

    private final Class<T> enumClass;

    protected EnumCodeConverter(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public Integer convertToDatabaseColumn(T attribute) {
        return attribute != null ? attribute.getCode() : null;
    }

    @Override
    public T convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getCode() == dbData)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid enum code: " + dbData));
    }
}