package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumCodeConverter;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.SenderType;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SenderTypeConverter extends EnumCodeConverter<SenderType> {
    public SenderTypeConverter() {
        super(SenderType.class);
    }
}
