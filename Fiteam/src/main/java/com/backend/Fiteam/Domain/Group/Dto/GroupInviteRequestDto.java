package com.backend.Fiteam.Domain.Group.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class GroupInviteRequestDto {

    @Schema(description = "초대할 그룹 ID", example = "3")
    private Integer groupId;

    @Schema(description = "초대할 사용자 이메일 목록", example = "[\"user1@example.com\", \"user2@example.com\"]")
    private List<String> emails;
}
