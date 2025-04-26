package com.backend.Fiteam.User.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserCardDto {
    private final String code;
    private final String name;
    private final String summary;
    private final String teamStrength;
    private final String caution;
    private final String bestMatchCode;
    private final String bestMatchReason;
    private final String worstMatchCode;
    private final String worstMatchReason;
    private final String details;
}
