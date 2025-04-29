package com.backend.Fiteam.Domain.Group.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupMemberResponseDto {
    private Integer userId;
    private String userName;
    private Integer cardId1;
    private String teamStatus;  // 참여 상태
    private String position;    // 직무 (PM, 디자이너 등)
}
