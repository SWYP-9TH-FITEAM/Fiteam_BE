// MyTeamMemberDto.java
package com.backend.Fiteam.Domain.Team.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamMemberDto {
    @Schema(description = "유저 ID", example = "3")
    private Integer userId;

    @Schema(description = "유저 이름", example = "김코딩")
    private String userName;

    @Schema(description = "프로필 이미지 URL", example = "https://…/profile.jpg")
    private String profileImgUrl;

    @Schema(description = "포지션", example = "백엔드")
    private String position;
}
