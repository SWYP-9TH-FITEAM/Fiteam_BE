package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumLabelDeserializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.SenderType;

public class SenderTypeDeserializer extends EnumLabelDeserializer<SenderType> {
    public SenderTypeDeserializer() {
        super(SenderType.class);
    }
}
