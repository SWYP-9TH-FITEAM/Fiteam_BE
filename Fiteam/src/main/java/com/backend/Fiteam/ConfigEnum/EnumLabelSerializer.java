package com.backend.Fiteam.ConfigEnum;

import com.backend.Fiteam.ConfigEnum.EnumType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class EnumLabelSerializer<T extends Enum<T> & EnumType> extends JsonSerializer<T> {
    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getLabel());
    }
}