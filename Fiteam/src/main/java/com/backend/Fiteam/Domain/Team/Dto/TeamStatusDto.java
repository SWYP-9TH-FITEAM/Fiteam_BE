// src/main/java/com/backend/Fiteam/Domain/Team/Dto/MyTeamResponseDto.java
package com.backend.Fiteam.Domain.Team.Dto;

import com.backend.Fiteam.ConfigEnum.Custom.TeamStatusDeserializer;
import com.backend.Fiteam.ConfigEnum.EnumLabelSerializer;
import com.backend.Fiteam.ConfigEnum.GlobalEnum.TeamStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @Schema(description = "팀 상태 (예: 모집중, 모집마감 등)", example = "모집중")
    @JsonSerialize(using = EnumLabelSerializer.class)
    @JsonDeserialize(using = TeamStatusDeserializer.class)
    private TeamStatus teamStatus;

    @Schema(description = "팀 멤버 리스트")
    private List<TeamMemberDto> members;
}
