package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumCodeConverter;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamRequestStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StatusEnumConverter extends EnumCodeConverter<TeamRequestStatus> {
    public StatusEnumConverter() {
        super(TeamRequestStatus.class);
    }
}
