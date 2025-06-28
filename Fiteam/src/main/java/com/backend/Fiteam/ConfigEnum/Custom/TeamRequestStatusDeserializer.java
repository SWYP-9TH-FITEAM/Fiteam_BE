package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumLabelDeserializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamRequestStatus;

public class TeamRequestStatusDeserializer extends EnumLabelDeserializer<TeamRequestStatus> {
    public TeamRequestStatusDeserializer() {
        super(TeamRequestStatus.class);
    }
}