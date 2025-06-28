package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumLabelDeserializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamRequestStatus;

public class StatusEnumDeserializer extends EnumLabelDeserializer<TeamRequestStatus> {
    public StatusEnumDeserializer() {
        super(TeamRequestStatus.class);
    }
}