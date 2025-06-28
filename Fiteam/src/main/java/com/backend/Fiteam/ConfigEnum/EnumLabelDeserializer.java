package com.backend.Fiteam.ConfigEnum;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Arrays;

public class EnumLabelDeserializer<T extends Enum<T> & EnumType> extends JsonDeserializer<T> {

    private final Class<T> enumClass;

    public EnumLabelDeserializer(Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String label = p.getText();
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(e -> e.getLabel().equalsIgnoreCase(label) || e.name().equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum label: " + label));
    }
}