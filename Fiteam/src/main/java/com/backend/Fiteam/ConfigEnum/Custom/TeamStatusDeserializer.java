package com.backend.Fiteam.ConfigEnum.Custom;

import com.backend.Fiteam.ConfigEnum.EnumLabelDeserializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;

public class TeamStatusDeserializer extends EnumLabelDeserializer<TeamStatus> {
    public TeamStatusDeserializer() {
        super(TeamStatus.class);
    }
}
