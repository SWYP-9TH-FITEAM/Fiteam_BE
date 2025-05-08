package com.backend.Fiteam.Domain.Chat.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "유저 접속 상태 정보")
public class UserPresenceDto {
    @Schema(description = "유저 아이디", example = "42")
    private Integer userId;
    @Schema(description = "온라인 여부", example = "true")
    private Boolean online;
}
