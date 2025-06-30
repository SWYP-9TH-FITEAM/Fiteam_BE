package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumCodeConverter;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.NotificationEventType;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NotificationEventTypeConverter extends EnumCodeConverter<NotificationEventType> {
    public NotificationEventTypeConverter() {
        super(NotificationEventType.class);
    }
}

