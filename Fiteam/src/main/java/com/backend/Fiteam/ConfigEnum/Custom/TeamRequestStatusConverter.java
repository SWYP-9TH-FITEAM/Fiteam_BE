package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumCodeConverter;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamRequestStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TeamRequestStatusConverter extends EnumCodeConverter<TeamRequestStatus> {
    public TeamRequestStatusConverter() {
        super(TeamRequestStatus.class);
    }
}
