package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumCodeConverter;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TeamStatusConverter extends EnumCodeConverter<TeamStatus> {
    public TeamStatusConverter() {
        super(TeamStatus.class);
    }
}
