package com.backend.Fiteam.Domain.Team.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class TeamRequestDto {

    @Schema(description = "그룹 ID", example = "1")
    private Integer groupId;

    @Schema(description = "받는 유저 ID", example = "5")
    private Integer receiverId;
}
