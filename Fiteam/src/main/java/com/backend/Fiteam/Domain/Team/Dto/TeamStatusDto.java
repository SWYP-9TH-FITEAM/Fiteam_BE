// src/main/java/com/backend/Fiteam/Domain/Team/Dto/MyTeamResponseDto.java
package com.backend.Fiteam.Domain.Team.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "내 팀 구성 및 팀 정보 응답 DTO")
@Getter
@Builder
public class TeamStatusDto {
    @Schema(description = "팀 ID", example = "42")
    private Integer teamId;

    @Schema(description = "팀장(마스터) 사용자 ID", example = "7")
    private Integer masterUserId;

    @Schema(description = "팀 상태", example = "모집종료")
    private String teamStatus;

    @Schema(description = "팀 멤버 리스트")
    private List<TeamMemberDto> members;
}
