package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumLabelDeserializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.NotificationEventType;

public class NotificationEventTypeDeserializer extends
        EnumLabelDeserializer<NotificationEventType> {
    public NotificationEventTypeDeserializer() {
        super(NotificationEventType.class);
    }
}
