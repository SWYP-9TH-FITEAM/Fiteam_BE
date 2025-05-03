package com.backend.Fiteam.Domain.User.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
public class UserGroupStatusDto {
    @Schema(description = "그룹 ID", example = "18")
    private Integer groupId;

    @Schema(description = "그룹 이름", example = "스위프 9기")
    private String groupName;

    @Schema(description = "초대(요청) 받은 시간", example = "2025-05-01T10:00:00")
    private Timestamp invitedAt;
}
